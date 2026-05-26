import { DriverStats, RaceResult } from './types.js';
import { dataService } from './dataService.js';

export const FEATURE_WEIGHTS = {
  recentForm: 0.25,
  avgPoints: 0.15,
  consistency: 0.10,
  teamStrength: 0.10,
  qualyForm: 0.10,
  circuitForm: 0.15,
  lapCompletion: 0.08,
  paceIndex: 0.07
} as const;

export interface FeatureVector {
  recentForm: number;
  avgPoints: number;
  consistency: number;
  teamStrength: number;
  qualyForm: number;
  circuitForm: number;
  lapCompletion: number;
  paceIndex: number;
}

export function lapTimeToSeconds(time: string): number | null {
  if (!time) return null;
  const parts = time.split(':');
  if (parts.length === 2) {
    const minutes = Number.parseFloat(parts[0]);
    const seconds = Number.parseFloat(parts[1]);
    if (!Number.isFinite(minutes) || !Number.isFinite(seconds)) return null;
    return minutes * 60 + seconds;
  }
  if (parts.length === 3) {
    const hours = Number.parseFloat(parts[0]);
    const minutes = Number.parseFloat(parts[1]);
    const seconds = Number.parseFloat(parts[2]);
    if (!Number.isFinite(hours) || !Number.isFinite(minutes) || !Number.isFinite(seconds)) return null;
    return hours * 3600 + minutes * 60 + seconds;
  }
  return null;
}

function calculateRecentForm(driver: DriverStats): number {
  if (!driver.recentPositions.length) return 0.5;
  const scores = driver.recentPositions.map(position => (21 - position) / 20);
  const weights = [0.35, 0.25, 0.20, 0.15, 0.05];
  return scores.reduce((sum, score, index) => sum + score * (weights[index] || 0.05), 0);
}

function calculateAvgPoints(driver: DriverStats): number {
  const races = driver.totalRaces ?? dataService.getResultsByDriver(driver.name).length;
  if (races === 0) return 0;
  return Math.min(driver.totalPoints / races / 25, 1);
}

function calculateTeamStrength(driver: DriverStats, allDrivers: DriverStats[]): number {
  const teamPoints = allDrivers.reduce<Record<string, number>>((acc, entry) => {
    acc[entry.team] = (acc[entry.team] || 0) + entry.totalPoints;
    return acc;
  }, {});
  const maxPoints = Math.max(...Object.values(teamPoints), 1);
  return (teamPoints[driver.team] || 0) / maxPoints;
}

function calculateQualyForm(results: RaceResult[]): number {
  if (!results.length) return 0.5;
  const gain = results.reduce((sum, result) => {
    const finish = result.position === 0 ? 20 : result.position;
    return sum + (result.grid - finish);
  }, 0) / results.length;
  return Math.min(Math.max((gain + 10) / 20, 0), 1);
}

function calculateCircuitForm(results: RaceResult[]): number {
  if (!results.length) return 0.5;

  const avgFinish = results.reduce((sum, result) => sum + (result.position === 0 ? 20 : result.position), 0) / results.length;
  const avgGridGain = results.reduce((sum, result) => {
    const finish = result.position === 0 ? 20 : result.position;
    return sum + (result.grid - finish);
  }, 0) / results.length;
  const finishScore = Math.max(0, (21 - avgFinish) / 20);
  const gainScore = Math.min(Math.max((avgGridGain + 5) / 10, 0), 1);

  return (finishScore * 0.75) + (gainScore * 0.25);
}

function calculateLapCompletion(results: RaceResult[]): number {
  if (!results.length) return 0.5;

  const completionRates = results.map(result => {
    const raceResults = dataService
      .getAllResults()
      .filter(entry => entry.season === result.season && entry.round === result.round);
    const maxLaps = Math.max(...raceResults.map(entry => entry.laps), 1);
    if (result.position === 0 || result.laps === 0) return 0;
    return Math.min(result.laps / maxLaps, 1);
  });

  return completionRates.reduce((sum, rate) => sum + rate, 0) / completionRates.length;
}

function calculatePaceIndex(results: RaceResult[]): number {
  const paceSamples = results
    .map(result => lapTimeToSeconds(result.fastestLap))
    .filter((value): value is number => value !== null);

  if (paceSamples.length === 0) return 0.5;

  const avgPace = paceSamples.reduce((sum, pace) => sum + pace, 0) / paceSamples.length;
  const fieldPaceSamples = results.flatMap(result =>
    dataService
      .getAllResults()
      .filter(entry => entry.season === result.season && entry.round === result.round)
      .map(entry => lapTimeToSeconds(entry.fastestLap))
      .filter((value): value is number => value !== null)
  );

  if (!fieldPaceSamples.length) return 0.5;

  const fieldAvg = fieldPaceSamples.reduce((sum, pace) => sum + pace, 0) / fieldPaceSamples.length;
  const delta = fieldAvg - avgPace;
  return Math.min(Math.max((delta + 2) / 4, 0), 1);
}

export function buildFeatureVector(
  driver: DriverStats,
  allDrivers: DriverStats[],
  circuit?: string
): FeatureVector {
  const allResults = dataService.getResultsByDriver(driver.name);
  const circuitResults = circuit
    ? dataService.getResultsByDriverAndCircuit(driver.name, circuit)
    : allResults;

  return {
    recentForm: calculateRecentForm(driver),
    avgPoints: calculateAvgPoints(driver),
    consistency: driver.consistency,
    teamStrength: calculateTeamStrength(driver, allDrivers),
    qualyForm: calculateQualyForm(allResults),
    circuitForm: calculateCircuitForm(circuitResults),
    lapCompletion: calculateLapCompletion(allResults),
    paceIndex: calculatePaceIndex(circuitResults.length ? circuitResults : allResults)
  };
}

export function scoreFeatures(features: FeatureVector): number {
  return (
    features.recentForm * FEATURE_WEIGHTS.recentForm +
    features.avgPoints * FEATURE_WEIGHTS.avgPoints +
    features.consistency * FEATURE_WEIGHTS.consistency +
    features.teamStrength * FEATURE_WEIGHTS.teamStrength +
    features.qualyForm * FEATURE_WEIGHTS.qualyForm +
    features.circuitForm * FEATURE_WEIGHTS.circuitForm +
    features.lapCompletion * FEATURE_WEIGHTS.lapCompletion +
    features.paceIndex * FEATURE_WEIGHTS.paceIndex
  );
}

export function explainFeatures(driver: DriverStats, features: FeatureVector, circuit?: string): string {
  if (circuit && features.circuitForm > 0.7) {
    return `${driver.name} has strong historical form at ${circuit}.`;
  }
  if (circuit && features.circuitForm < 0.4) {
    return `${driver.name} has underperformed at ${circuit} relative to other circuits.`;
  }
  if (features.paceIndex > 0.75) {
    return `${driver.name} shows consistently strong lap-time telemetry on comparable circuits.`;
  }
  if (features.lapCompletion < 0.45) {
    return `${driver.name} carries elevated reliability risk based on historical lap-completion rates.`;
  }
  if (features.recentForm > 0.75) {
    return `${driver.name} enters with strong recent form across the training window.`;
  }
  return `${driver.name} projects as a variable performer on this circuit profile.`;
}

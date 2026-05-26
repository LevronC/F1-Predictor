export type Screen = 'home' | 'drivers' | 'teams' | 'predict' | 'backtest';

export interface PredictionResult {
  driverName: string;
  teamName: string;
  totalScore: number;
  confidence: string;
  explanation: string;
  breakdown: {
    recentForm: number;
    avgPoints: number;
    consistency: number;
    teamStrength: number;
    qualyForm: number;
    circuitForm: number;
    lapCompletion: number;
    paceIndex: number;
  };
}

export interface SimulationResult {
  driverName: string;
  winProbability: number;
  podiumProbability: number;
  top10Probability: number;
  averagePosition: number;
  positionFrequency: Record<number, number>;
}

export interface BacktestReport {
  season: number;
  accuracy: number;
  avgError: number;
  roundsEvaluated: number;
}

export interface RaceResult {
  season: number;
  round: number;
  circuit: string;
  date: string;
  driverName: string;
  constructorName: string;
  grid: number;
  position: number;
  points: number;
  laps: number;
  status: string;
  fastestLap: string;
}

export interface DatasetStats {
  seasonCount: number;
  yearStart: number;
  yearEnd: number;
  seasons: number[];
  raceCount: number;
  resultCount: number;
  driverCount: number;
  circuitCount: number;
  featureCount: number;
}

export interface DriverStats {
  name: string;
  team: string;
  totalRaces?: number;
  wins: number;
  podiums: number;
  totalPoints: number;
  avgPosition: number;
  consistency: number;
  recentPositions: number[];
  image?: string;
}

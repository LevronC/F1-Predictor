import { DriverStats, PredictionResult, SimulationResult } from './types.js';
import { predictionEngine } from './predictionEngine.js';

export function runSimulation(
  drivers: DriverStats[],
  iterations: number,
  gridInfluence: number = 74,
  circuit?: string
): SimulationResult[] {
  const basePredictions = predictionEngine.predict(drivers, gridInfluence, circuit);
  const totals = new Map<string, {
    wins: number;
    podiums: number;
    top10: number;
    positionSum: number;
    positionFrequency: Record<number, number>;
  }>();

  basePredictions.forEach(prediction => {
    totals.set(prediction.driverName, {
      wins: 0,
      podiums: 0,
      top10: 0,
      positionSum: 0,
      positionFrequency: {}
    });
  });

  for (let iteration = 0; iteration < iterations; iteration++) {
    const ranked = basePredictions
      .map((prediction): PredictionResult => ({
        ...prediction,
        totalScore: prediction.totalScore + (Math.random() - 0.5) * 1.5
      }))
      .sort((a, b) => b.totalScore - a.totalScore);

    ranked.forEach((result, index) => {
      const position = index + 1;
      const driverTotals = totals.get(result.driverName);
      if (!driverTotals) return;

      driverTotals.positionSum += position;
      driverTotals.positionFrequency[position] = (driverTotals.positionFrequency[position] || 0) + 1;
      if (position === 1) driverTotals.wins += 1;
      if (position <= 3) driverTotals.podiums += 1;
      if (position <= 10) driverTotals.top10 += 1;
    });
  }

  return Array.from(totals.entries())
    .map(([driverName, stats]) => ({
      driverName,
      winProbability: stats.wins / iterations,
      podiumProbability: stats.podiums / iterations,
      top10Probability: stats.top10 / iterations,
      averagePosition: stats.positionSum / iterations,
      positionFrequency: stats.positionFrequency
    }))
    .sort((a, b) => b.winProbability - a.winProbability);
}

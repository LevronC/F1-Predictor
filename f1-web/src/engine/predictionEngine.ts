import { DriverStats, PredictionResult } from './types.js';
import { buildFeatureVector, explainFeatures, scoreFeatures } from './featureEngine.js';

export class PredictionEngine {
  predict(drivers: DriverStats[], gridInfluence: number = 0.74, circuit?: string): PredictionResult[] {
    const gridWeight = Math.max(0, Math.min(1, gridInfluence > 1 ? gridInfluence / 100 : gridInfluence));

    return drivers.map(driver => {
      const features = buildFeatureVector(driver, drivers, circuit);
      const modelScore = scoreFeatures(features);
      const finalScore = (modelScore * (1 - gridWeight)) + (features.qualyForm * gridWeight);

      return {
        driverName: driver.name,
        teamName: driver.team,
        totalScore: Math.min(finalScore * 10, 10),
        confidence: this.getConfidence(finalScore),
        explanation: explainFeatures(driver, features, circuit),
        breakdown: features
      };
    }).sort((a, b) => b.totalScore - a.totalScore);
  }

  private getConfidence(score: number): string {
    if (score > 0.8) return 'Very High';
    if (score > 0.7) return 'High';
    if (score > 0.5) return 'Moderate';
    return 'Low';
  }
}

export const predictionEngine = new PredictionEngine();

import { DriverStats, PredictionResult } from './types';
import { dataService } from './dataService';

export class PredictionEngine {
  predict(drivers: DriverStats[], gridInfluence: number = 0.74): PredictionResult[] {
    return drivers.map(driver => {
      const recentForm = this.calculateRecentForm(driver);
      const avgPoints = Math.min(driver.totalPoints / (driver.wins + 20) / 25, 1);
      const consistency = driver.consistency;
      const teamStrength = this.calculateTeamStrength(driver);
      const qualyForm = this.calculateQualyForm(driver);

      // Weighted Score
      const baseScore = (recentForm * 0.35) + 
                        (avgPoints * 0.25) + 
                        (consistency * 0.15) + 
                        (teamStrength * 0.15) + 
                        (qualyForm * 0.10);

      // Apply Grid Influence (Simplified)
      const finalScore = baseScore * (1 - (gridInfluence / 10)) + (gridInfluence / 10) * 0.8;

      return {
        driverName: driver.name,
        teamName: driver.team,
        totalScore: Math.min(finalScore * 10, 10),
        confidence: this.getConfidence(finalScore),
        explanation: this.generateExplanation(driver, finalScore),
        breakdown: { recentForm, avgPoints, consistency, teamStrength, qualyForm }
      };
    }).sort((a, b) => b.totalScore - a.totalScore);
  }

  private calculateRecentForm(driver: DriverStats): number {
    if (!driver.recentPositions.length) return 0.5;
    const scores = driver.recentPositions.map(p => (21 - p) / 20);
    const weights = [0.35, 0.25, 0.20, 0.15, 0.05];
    return scores.reduce((sum, s, i) => sum + s * (weights[i] || 0.05), 0);
  }

  private calculateTeamStrength(driver: DriverStats): number {
    const teams = dataService.getAllDrivers().reduce((acc, d) => {
      acc[d.team] = (acc[d.team] || 0) + d.totalPoints;
      return acc;
    }, {} as Record<string, number>);
    
    const maxPoints = Math.max(...Object.values(teams));
    return (teams[driver.team] || 0) / maxPoints;
  }

  private calculateQualyForm(driver: DriverStats): number {
    const results = dataService.getResultsByDriver(driver.name);
    const gain = results.reduce((sum, r) => sum + (r.grid - (r.position || 20)), 0) / results.length;
    return Math.min(Math.max((gain + 10) / 20, 0), 1);
  }

  private getConfidence(score: number): string {
    if (score > 0.8) return 'Very High';
    if (score > 0.7) return 'High';
    if (score > 0.5) return 'Moderate';
    return 'Low';
  }

  private generateExplanation(driver: DriverStats, score: number): string {
    if (score > 0.8) return `${driver.name} is in elite form with exceptional consistency.`;
    if (score > 0.6) return `${driver.name} shows strong podium potential based on recent telemetry.`;
    return `${driver.name} performance metrics suggest high variability for this circuit.`;
  }
}

export const predictionEngine = new PredictionEngine();

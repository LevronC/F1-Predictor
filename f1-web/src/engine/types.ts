export type Screen = 'home' | 'drivers' | 'teams' | 'predict' | 'backtest';

export interface PredictionResult {
  driverName: string;
  teamName: string;
  totalScore: number;
  recentFormScore: number;
  avgPointsScore: number;
  consistencyScore: number;
  teamStrengthScore: number;
  qualifyingFormScore: number;
  headToHeadScore: number;
  confidenceLevel: string;
  explanation: string;
}

export interface SimulationResult {
  driverName: string;
  winProbability: number;
  podiumProbability: number;
  top10Probability: number;
  averagePosition: number;
  positionFrequency: Record<number, number>;
}

export interface DriverStats {
  name: string;
  totalRaces: int;
  wins: int;
  podiums: int;
  totalPoints: number;
  avgPosition: number;
  consistency: number;
  image?: string;
}

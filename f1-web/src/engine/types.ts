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
  status: string;
}

export interface DriverStats {
  name: string;
  team: string;
  totalPoints: number;
  wins: number;
  podiums: number;
  avgPosition: number;
  consistency: number;
  image: string;
  recentPositions: number[];
}

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
  };
}

export type Screen = 'home' | 'drivers' | 'teams' | 'races' | 'predict';

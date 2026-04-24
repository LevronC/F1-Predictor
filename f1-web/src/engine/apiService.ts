import { PredictionResult, SimulationResult, DriverStats } from './types';

export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  async getPredictions(): Promise<PredictionResult[]> {
    const response = await fetch(`${this.baseUrl}/predictions`);
    return response.json();
  }

  async runSimulation(iterations: number = 1000): Promise<SimulationResult[]> {
    const response = await fetch(`${this.baseUrl}/simulate?iterations=${iterations}`);
    return response.json();
  }

  async getBacktest(season: number): Promise<any> {
    const response = await fetch(`${this.baseUrl}/backtest/${season}`);
    return response.json();
  }
}

export const apiService = new ApiService();

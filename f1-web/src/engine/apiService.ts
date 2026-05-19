import { BacktestReport, PredictionResult, SimulationResult } from './types';

export class ApiService {
  private baseUrl = import.meta.env.VITE_API_URL ?? 'http://127.0.0.1:8080/api';

  private async fetchJson<T>(path: string, timeoutMs: number = 1500): Promise<T> {
    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(`${this.baseUrl}${path}`, { signal: controller.signal });
      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`);
      }
      return response.json() as Promise<T>;
    } finally {
      window.clearTimeout(timeoutId);
    }
  }

  async getPredictions(): Promise<PredictionResult[]> {
    return this.fetchJson<PredictionResult[]>('/predictions');
  }

  async runSimulation(iterations: number = 1000): Promise<SimulationResult[]> {
    return this.fetchJson<SimulationResult[]>(`/simulate?iterations=${iterations}`, 2000);
  }

  async getBacktest(season: number): Promise<BacktestReport> {
    return this.fetchJson<BacktestReport>(`/backtest/${season}`, 2000);
  }
}

export const apiService = new ApiService();

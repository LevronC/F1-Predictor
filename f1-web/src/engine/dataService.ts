import { RaceResult, DriverStats, DatasetStats } from './types.js';
import { aggregateDrivers, parseCSV } from './dataCore.js';
import { FEATURE_WEIGHTS } from './featureEngine.js';

export class DataService {
  private results: RaceResult[] = [];
  private drivers: Map<string, DriverStats> = new Map();

  async loadData() {
    const response = await fetch('/data.csv');
    const csvText = await response.text();
    this.loadFromCsv(csvText);
  }

  loadFromCsv(text: string) {
    this.results = parseCSV(text);
    this.drivers = aggregateDrivers(this.results);
  }

  getAllDrivers(): DriverStats[] {
    return Array.from(this.drivers.values()).sort((a, b) => b.totalPoints - a.totalPoints);
  }

  getDriver(name: string): DriverStats | undefined {
    return this.drivers.get(name);
  }

  getResultsByDriver(name: string): RaceResult[] {
    return this.results.filter(r => r.driverName === name);
  }

  getResultsByDriverAndCircuit(name: string, circuit: string): RaceResult[] {
    return this.results.filter(r => r.driverName === name && r.circuit === circuit);
  }

  getAllResults(): RaceResult[] {
    return this.results;
  }

  getCircuits(): string[] {
    return Array.from(new Set(this.results.map(r => r.circuit))).sort();
  }

  getTeams(): string[] {
    return Array.from(new Set(this.results.map(r => r.constructorName))).sort();
  }

  getDatasetStats(): DatasetStats {
    const seasons = Array.from(new Set(this.results.map(r => r.season))).sort((a, b) => a - b);
    const circuits = new Set(this.results.map(r => r.circuit));
    const races = new Set(this.results.map(r => `${r.season}-${r.round}`));

    return {
      seasonCount: seasons.length,
      yearStart: seasons[0] ?? 0,
      yearEnd: seasons[seasons.length - 1] ?? 0,
      seasons,
      raceCount: races.size,
      resultCount: this.results.length,
      driverCount: this.drivers.size,
      circuitCount: circuits.size,
      featureCount: Object.keys(FEATURE_WEIGHTS).length
    };
  }
}

export const dataService = new DataService();

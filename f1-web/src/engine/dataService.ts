import { RaceResult, DriverStats } from './types';
import { aggregateDrivers, parseCSV } from './dataCore';

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
}

export const dataService = new DataService();

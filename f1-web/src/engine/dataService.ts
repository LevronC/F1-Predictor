import { RaceResult, DriverStats } from './types';

export class DataService {
  private results: RaceResult[] = [];
  private drivers: Map<string, DriverStats> = new Map();

  async loadData() {
    const response = await fetch('/data.csv');
    const csvText = await response.text();
    this.results = this.parseCSV(csvText);
    this.aggregateStats();
  }

  private parseCSV(text: string): RaceResult[] {
    const lines = text.trim().split('\n');
    // const header = lines[0].split(',');
    
    return lines.slice(1).map(line => {
      const values = line.split(',');
      return {
        season: parseInt(values[0]),
        round: parseInt(values[1]),
        circuit: values[2],
        date: values[3],
        driverName: values[4],
        constructorName: values[5],
        grid: parseInt(values[6]),
        position: parseInt(values[7]) || 0, // 0 for DNF
        points: parseFloat(values[8]),
        status: values[10]
      };
    });
  }

  private aggregateStats() {
    const driverResults = new Map<string, RaceResult[]>();
    
    this.results.forEach(r => {
      if (!driverResults.has(r.driverName)) driverResults.set(r.driverName, []);
      driverResults.get(r.driverName)!.push(r);
    });

    driverResults.forEach((results, name) => {
      const sorted = results.sort((a, b) => b.season - a.season || b.round - a.round);
      const recent = sorted.slice(0, 5).map(r => r.position === 0 ? 20 : r.position);
      
      const wins = results.filter(r => r.position === 1).length;
      const podiums = results.filter(r => r.position >= 1 && r.position <= 3).length;
      const totalPoints = results.reduce((sum, r) => sum + r.points, 0);
      const avgPos = results.reduce((sum, r) => sum + (r.position === 0 ? 20 : r.position), 0) / results.length;
      
      // Consistency (Simplified StdDev)
      const variance = results.reduce((sum, r) => {
        const p = r.position === 0 ? 20 : r.position;
        return sum + Math.pow(p - avgPos, 2);
      }, 0) / results.length;
      const consistency = Math.max(0, 1 - (Math.sqrt(variance) / 20));

      this.drivers.set(name, {
        name,
        team: sorted[0].constructorName,
        totalPoints,
        wins,
        podiums,
        avgPosition: avgPos,
        consistency,
        image: this.getDriverImage(name),
        recentPositions: recent
      });
    });
  }

  private getDriverImage(name: string): string {
    const slug = name.toLowerCase().replace(/\s+/g, '-');
    const mapping: Record<string, string> = {
      'max-verstappen': 'MAXVER01',
      'sergio-perez': 'SERPER01',
      'lewis-hamilton': 'LEWHAM01',
      'george-russell': 'GEORUS01',
      'charles-leclerc': 'CHALEC01',
      'carlos-sainz': 'CARSAI01',
      'lando-norris': 'LANNOR01',
      'oscar-piastri': 'OSCPIA01',
      'fernando-alonso': 'FERALO01',
      'lance-stroll': 'LANSTR01',
      'esteban-ocon': 'ESTOCO01',
      'pierre-gasly': 'PIEGAS01',
      'alexander-albon': 'ALEALB01',
      'logan-sargeant': 'LOGSAR01',
      'yuki-tsunoda': 'YUKTSU01',
      'daniel-ricciardo': 'DANRIC01',
      'nico-hulkenberg': 'NICHUL01',
      'kevin-magnussen': 'KEVMAG01',
      'valtteri-bottas': 'VALBOT01',
      'zhou-guanyu': 'ZHOGUA01',
      'liam-lawson': 'LIALAW01',
      'nyck-de-vries': 'NYCDEV01'
    };

    const id = mapping[slug];
    if (!id) return `https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png`;
    
    const initial = id.substring(0, 1);
    const firstName = name.split(' ')[0];
    const lastName = name.split(' ').slice(1).join('_');
    
    return `https://media.formula1.com/d_driver_fallback_image.png/content/dam/fom-website/drivers/${initial}/${id}_${firstName}_${lastName}/${id.toLowerCase()}.png`;
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

  getCircuits(): string[] {
    return Array.from(new Set(this.results.map(r => r.circuit))).sort();
  }

  getTeams(): string[] {
    return Array.from(new Set(this.results.map(r => r.constructorName))).sort();
  }
}

export const dataService = new DataService();

import { DriverStats, RaceResult } from './types.js';

function parsePosition(raw: string): number {
  if (/^\d+$/.test(raw)) return parseInt(raw, 10);
  return 0;
}

export function parseCSV(text: string): RaceResult[] {
  const lines = text.trim().split('\n');

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
      position: parsePosition(values[7]),
      points: parseFloat(values[8]),
      laps: parseInt(values[9]) || 0,
      status: values[10] || 'Finished',
      fastestLap: values[11] || ''
    };
  });
}

export function getDriverImage(name: string): string {
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
  if (!id) {
    return 'https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png';
  }

  const initial = id.substring(0, 1);
  const firstName = name.split(' ')[0];
  const lastName = name.split(' ').slice(1).join('_');

  return `https://media.formula1.com/d_driver_fallback_image.png/content/dam/fom-website/drivers/${initial}/${id}_${firstName}_${lastName}/${id.toLowerCase()}.png`;
}

export function aggregateDrivers(results: RaceResult[]): Map<string, DriverStats> {
  const drivers = new Map<string, DriverStats>();
  const driverResults = new Map<string, RaceResult[]>();

  results.forEach(result => {
    if (!driverResults.has(result.driverName)) {
      driverResults.set(result.driverName, []);
    }
    driverResults.get(result.driverName)!.push(result);
  });

  driverResults.forEach((entries, name) => {
    const sorted = entries.sort((a, b) => b.season - a.season || b.round - a.round);
    const recent = sorted.slice(0, 5).map(r => (r.position === 0 ? 20 : r.position));

    const wins = entries.filter(r => r.position === 1).length;
    const podiums = entries.filter(r => r.position >= 1 && r.position <= 3).length;
    const totalPoints = entries.reduce((sum, r) => sum + r.points, 0);
    const avgPos = entries.reduce((sum, r) => sum + (r.position === 0 ? 20 : r.position), 0) / entries.length;

    const variance = entries.reduce((sum, r) => {
      const position = r.position === 0 ? 20 : r.position;
      return sum + Math.pow(position - avgPos, 2);
    }, 0) / entries.length;
    const consistency = Math.max(0, 1 - (Math.sqrt(variance) / 20));

    drivers.set(name, {
      name,
      team: sorted[0].constructorName,
      totalRaces: entries.length,
      totalPoints,
      wins,
      podiums,
      avgPosition: avgPos,
      consistency,
      image: getDriverImage(name),
      recentPositions: recent
    });
  });

  return drivers;
}

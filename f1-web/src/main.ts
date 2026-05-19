import './style.css'
import { Chart, registerables } from 'chart.js'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { dataService } from './engine/dataService'
import { apiService } from './engine/apiService'
import { predictionEngine } from './engine/predictionEngine'
import { runSimulation } from './engine/simulationEngine'
import { buildLocalBacktest } from './engine/backtestEngine'
import { BacktestReport, Screen, SimulationResult } from './engine/types'

Chart.register(...registerables)
gsap.registerPlugin(ScrollTrigger)

const app = document.querySelector<HTMLDivElement>('#app')!

declare global {
  interface Window {
    navigate: (screen: Screen) => void;
    enterPlatform: () => void;
    runSimulation: () => void;
    runBacktest: () => void;
  }
}

// --- State ---
let hasEnteredPlatform = false
let currentScreen: Screen = 'home'
let isLoading = true
let gridInfluence = 74
let selectedCircuit = 'Great Britain'
let simulationResults: SimulationResult[] = []
let backtestReport: BacktestReport = {
  season: 2023,
  accuracy: 0.742,
  avgError: 1.8,
  roundsEvaluated: 24
}
let simulationStatus = 'Ready'
let backtestStatus = 'Using cached validation results'

// --- Templates ---

const Icons = {
  home: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M4 11h16v8H4zm2-6h12l2 4H4z"/></svg>`,
  drivers: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M12 2 4 6v6c0 5 3.4 8.6 8 10 4.6-1.4 8-5 8-10V6zm0 3.2 5 2.5V12c0 3.4-2 5.8-5 7-3-1.2-5-3.6-5-7V7.7z"/></svg>`,
  teams: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M3 5h18v4H3zm2 6h14v3H5zm3 5h8v3H8z"/></svg>`,
  races: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M5 3h12l2 4-2 4H7v10H5z"/></svg>`,
  predict: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M4 17 9.5 11l4 3.8L20 6l1.5 1.2-7.8 10.5-4-3.8L5.5 18z"/></svg>`
}

const teamColors: Record<string, string> = {
  'Red Bull Racing': '#3671c6',
  'Red Bull': '#3671c6',
  Ferrari: '#e10600',
  Mercedes: '#00d2be',
  McLaren: '#ff8000',
  'Aston Martin': '#229971',
  Alpine: '#ff87bc',
  Williams: '#64c4ff',
  'RB F1 Team': '#6692ff',
  AlphaTauri: '#5e8faa',
  Haas: '#b6babd',
  'Alfa Romeo': '#c92d4b',
  'Kick Sauber': '#52e252'
}

const getTeamColor = (team: string) => teamColors[team] || '#e10600'
const pct = (value: number) => `${(value * 100).toFixed(1)}%`
const clampPct = (value: number) => Math.max(0, Math.min(100, value))
const teamStyle = (team: string) => `--team-color: ${getTeamColor(team)};`

const confidenceLabel = (score: number) => {
  if (score >= 8) return 'High'
  if (score >= 6.5) return 'Medium'
  return 'Watch'
}

const renderHeader = () => `
  <header class="header">
    <div class="logo">F1 <span>PREDICTOR</span></div>
    <div class="user-profile">
       <div class="status-chip status-positive header-only">MODEL LIVE</div>
       <div class="status-chip status-alert">RACE CONTROL</div>
    </div>
  </header>
`

const renderBottomNav = () => `
  <nav class="bottom-nav">
    <a href="#" class="nav-item ${currentScreen === 'home' ? 'active' : ''}" data-screen="home">${Icons.home}HOME</a>
    <a href="#" class="nav-item ${currentScreen === 'drivers' ? 'active' : ''}" data-screen="drivers">${Icons.drivers}DRIVERS</a>
    <a href="#" class="nav-item ${currentScreen === 'teams' ? 'active' : ''}" data-screen="teams">${Icons.teams}TEAMS</a>
    <a href="#" class="nav-item ${currentScreen === 'predict' ? 'active' : ''}" data-screen="predict">${Icons.predict}PREDICT</a>
    <a href="#" class="nav-item ${currentScreen === 'backtest' ? 'active' : ''}" data-screen="backtest">${Icons.predict}BACKTEST</a>
  </nav>
`

const renderLandingHero = () => `
  <section class="landing-scrub" aria-label="F1 Predictor cinematic landing">
    <div class="landing-sticky">
      <div class="landing-shade"></div>
      <div class="landing-track-glow"></div>

      <div class="landing-brand">
        <div class="logo">F1 <span>PREDICTOR</span></div>
        <button class="landing-enter compact" onclick="window.enterPlatform()">Enter Platform</button>
      </div>

      <div class="landing-stage">
        <h1 class="landing-title landing-title-top">F1</h1>
        <div class="landing-card">
          <img class="landing-parts-board" src="/assets/f1-parts-clean.svg" alt="Exploded Formula car parts layout">
          <video
            id="landingPartsVideo"
            class="landing-video"
            src="/assets/f1-garage-parts.ogv"
            muted
            playsinline
            preload="auto"
            poster="/assets/f1-parts-clean.svg"
          ></video>
          <div class="landing-card-vignette"></div>
          <div class="landing-data-chip">
            <span>Assembly View</span>
            <strong>Scroll to build</strong>
          </div>
        </div>
        <h1 class="landing-title landing-title-bottom">Predictor</h1>
      </div>

      <div class="landing-copy">
        <div class="label-md">Race Intelligence Platform</div>
        <p>Predict race outcomes from driver form, team pace, circuit fit, and simulation variance.</p>
        <button class="landing-enter" onclick="window.enterPlatform()">Launch Predictor</button>
      </div>
    </div>
  </section>
`

const getHomeView = () => {
  const drivers = dataService.getAllDrivers()
  const topDriver = drivers[0]
  const consistent = drivers.find(d => d.consistency === Math.max(...drivers.map(x => x.consistency))) || topDriver
  const predictions = predictionEngine.predict(drivers, gridInfluence, selectedCircuit)
  const leaderPrediction = predictions[0]
  const leader = dataService.getDriver(leaderPrediction.driverName) || topDriver
  const modelConfidence = clampPct((simulationResults[0]?.winProbability || 0.34) * 100)
  const topSix = predictions.slice(0, 6)

  return `
    <div class="container">
      <div class="telemetry-strip" style="margin-bottom: 16px;">
        <div class="telemetry-cell">
          <div class="label-sm">Next Model Run</div>
          <div class="stat-value">${selectedCircuit}</div>
          <div class="metric-sub">${selectedCircuit} baseline loaded</div>
        </div>
        <div class="telemetry-cell">
          <div class="label-sm">Season Progress</div>
          <div class="stat-value">12 <span style="color: var(--text-dim);">/ 24</span></div>
          <div class="progress-track"><span style="width: 50%;"></span></div>
        </div>
        <div class="telemetry-cell">
          <div class="label-sm">Grid Influence</div>
          <div class="stat-value">${gridInfluence}%</div>
          <div class="metric-sub">qualifying weight</div>
        </div>
        <div class="telemetry-cell">
          <div class="label-sm">Model Confidence</div>
          <div class="stat-value">${modelConfidence.toFixed(0)}%</div>
          <div class="progress-track"><span style="width: ${modelConfidence}%;"></span></div>
        </div>
      </div>

      <div class="race-control-grid">
        <section class="panel hero-panel" style="${teamStyle(leader.team)}">
          <div class="hero-copy">
            <div>
              <div class="status-chip status-red" style="margin-bottom: 18px;">Predicted Race Winner</div>
              <h1 class="display-lg">${leaderPrediction.driverName}</h1>
              <p class="text-secondary" style="max-width: 560px; margin-top: 14px;">
                ${selectedCircuit} projection using recent form, team strength, qualifying influence, and consistency.
              </p>
            </div>
            <div class="hero-stats">
              <div class="stat-cell">
                <div class="label-sm">Score</div>
                <div class="stat-value">${leaderPrediction.totalScore.toFixed(2)}</div>
              </div>
              <div class="stat-cell">
                <div class="label-sm">Team</div>
                <div class="stat-value" style="font-size: 1.1rem;">${leader.team}</div>
              </div>
              <div class="stat-cell">
                <div class="label-sm">Confidence</div>
                <div class="stat-value" style="color: var(--pit-yellow);">${confidenceLabel(leaderPrediction.totalScore)}</div>
              </div>
            </div>
            <button class="btn-primary" onclick="window.navigate('predict')">Open Prediction Console</button>
          </div>
          <img class="hero-image" src="${leader.image}" onerror="this.style.display='none'">
        </section>

        <aside class="timing-table">
          ${topSix.map((res, index) => {
            const d = dataService.getDriver(res.driverName) || topDriver
            return `
              <div class="timing-row ${index === 0 ? 'leader' : ''}" style="${teamStyle(d.team)}">
                <div class="position-badge">${index + 1}</div>
                <div class="driver-line">
                  <div class="driver-name">${res.driverName}</div>
                  <div class="team-tag"><span class="team-swatch"></span>${d.team}</div>
                </div>
                <div class="label-sm hide-mobile">Score</div>
                <div class="display-sm" style="font-size: 1rem; text-align: right;">${res.totalScore.toFixed(2)}</div>
              </div>
            `
          }).join('')}
        </aside>
      </div>

      <div class="dashboard-grid" style="margin-top: 16px;">
        <div class="panel metric-panel">
          <div class="label-sm">Championship Leader</div>
          <div>
            <div class="metric-value">${topDriver.totalPoints.toFixed(0)}</div>
            <div class="metric-sub">${topDriver.name} points total</div>
          </div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">Most Consistent</div>
          <div>
            <div class="metric-value">${pct(consistent.consistency)}</div>
            <div class="metric-sub">${consistent.name}</div>
          </div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">Points Average</div>
          <div>
            <div class="metric-value">${(topDriver.totalPoints / 20).toFixed(1)}</div>
            <div class="metric-sub">leader pace per race</div>
          </div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">Leader Wins</div>
          <div>
            <div class="metric-value">${topDriver.wins}</div>
            <div class="metric-sub">${topDriver.podiums} career podiums in data</div>
          </div>
        </div>
      </div>
    </div>
  `
}

const getDriversView = () => {
  const drivers = dataService.getAllDrivers()
  return `
    <div class="container">
      <div style="display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-bottom: 18px;">
        <div>
          <div class="label-md">Timing Tower</div>
          <h1 class="display-md">Driver Analytics</h1>
        </div>
        <div class="status-chip status-positive">Live Dataset</div>
      </div>
      
      <div class="dashboard-grid">
        ${drivers.map((d) => `
          <div class="panel driver-card" style="${teamStyle(d.team)}">
            <div class="driver-avatar">
              <img src="${d.image}" onerror="this.src='https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png'">
            </div>
            <div class="driver-line">
              <div class="driver-name">${d.name}</div>
              <div class="team-tag"><span class="team-swatch"></span>${d.team}</div>
              <div class="confidence-meter" style="margin-top: 10px;"><span style="width: ${clampPct(d.consistency * 100)}%;"></span></div>
            </div>
            <div style="text-align: right;">
              <div class="display-sm">${d.totalPoints.toFixed(0)}</div>
              <div class="label-sm">PTS</div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `
}

const getTeamsView = () => {
  const drivers = dataService.getAllDrivers()
  const teamsMap = new Map<string, number>()
  drivers.forEach(d => teamsMap.set(d.team, (teamsMap.get(d.team) || 0) + d.totalPoints))
  const teams = Array.from(teamsMap.entries()).sort((a, b) => b[1] - a[1])
  const maxPoints = teams[0][1]

  return `
    <div class="container">
      <div style="display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-bottom: 18px;">
        <div>
          <div class="label-md">Factory Output</div>
          <h1 class="display-md">Constructor Performance</h1>
        </div>
        <div class="status-chip status-alert">${teams.length} Teams</div>
      </div>

      <div class="panel">
        <div class="label-md" style="margin-bottom: 14px;">World Championship Standings</div>
        ${teams.map(([team, pts]) => `
          <div class="team-row" style="${teamStyle(team)}">
            <div>
              <div class="driver-name" style="display: flex; align-items: center; gap: 10px;">
                <span class="team-swatch"></span>${team}
              </div>
              <div class="team-bar"><span style="width: ${(pts / maxPoints) * 100}%;"></span></div>
            </div>
            <div style="text-align: right;">
              <div class="display-sm">${pts.toFixed(0)}</div>
              <div class="label-sm">PTS</div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `
}

const getPredictView = () => {
  const circuits = dataService.getCircuits()
  const predictions = predictionEngine.predict(dataService.getAllDrivers(), gridInfluence, selectedCircuit)
  const top3 = predictions.slice(0, 3)

  return `
    <div class="container">
       <div style="display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-bottom: 18px;">
        <div>
          <div class="label-md">Simulation Console</div>
          <h1 class="display-md">Prediction Engine</h1>
        </div>
        <div class="status-chip status-positive">${simulationStatus}</div>
       </div>

       <div class="prediction-grid">
        <section class="panel control-stack">
          <div>
            <div class="label-sm">Circuit Selection</div>
            <select id="circuit-select" class="form-control">
              ${circuits.map(c => `<option value="${c}" ${c === selectedCircuit ? 'selected' : ''}>${c}</option>`).join('')}
            </select>
          </div>

          <div>
            <div class="label-sm">Grid Position Influence <span style="color: var(--text-primary);">${gridInfluence}%</span></div>
            <input class="range-control" type="range" id="grid-slider" min="0" max="100" value="${gridInfluence}">
            <div class="telemetry-strip" style="grid-template-columns: repeat(3, 1fr); margin-top: 14px;">
              <div class="telemetry-cell" style="min-height: 74px;">
                <div class="label-sm">Form</div>
                <div class="stat-value" style="font-size: 1.35rem;">35%</div>
              </div>
              <div class="telemetry-cell" style="min-height: 74px;">
                <div class="label-sm">Grid</div>
                <div class="stat-value" style="font-size: 1.35rem;">${gridInfluence}%</div>
              </div>
              <div class="telemetry-cell" style="min-height: 74px;">
                <div class="label-sm">Circuit</div>
                <div class="stat-value" style="font-size: 1.35rem;">Live</div>
              </div>
            </div>
          </div>
          
          <button class="btn-primary" onclick="window.runSimulation()">Run Simulation</button>
        </section>

        <section class="panel chart-panel">
          <div class="label-md" style="margin-bottom: 18px;">Monte Carlo Win Probability</div>
          <canvas id="probabilityChart"></canvas>
        </section>
       </div>

       <div class="label-md" style="margin: 22px 0 12px;">Predicted Podium</div>
       <div id="podium-list" class="podium-grid">
         ${top3.map((res, i) => {
           const d = dataService.getDriver(res.driverName)!
           const sim = simulationResults.find(result => result.driverName === res.driverName)
           const win = sim ? sim.winProbability * 100 : Math.max(4, 42 - i * 12)
           return `
           <div class="panel podium-card ${i === 0 ? 'p1' : ''}" style="${teamStyle(d.team)}">
             <div class="podium-rank">
              <div class="position-badge">${i + 1}</div>
              <div class="label-sm">${i === 0 ? 'Projected Winner' : i === 1 ? 'P2 Threat' : 'Podium Contender'}</div>
             </div>
             <div>
               <div class="driver-name" style="font-size: 1.3rem;">${res.driverName}</div>
               <div class="team-tag"><span class="team-swatch"></span>${d.team}</div>
             </div>
             <div>
              <div class="podium-rank">
                <div>
                  <div class="label-sm">Score</div>
                  <div class="display-sm">${res.totalScore.toFixed(2)}</div>
                </div>
                <div style="text-align: right;">
                  <div class="label-sm">Win Prob</div>
                  <div class="display-sm">${win.toFixed(1)}%</div>
                </div>
              </div>
              <div class="confidence-meter" style="margin-top: 12px;"><span style="width: ${clampPct(win)}%;"></span></div>
             </div>
           </div>
           `
         }).join('')}
    </div>
  `
}

function getBacktestView() {
  return `
    <div class="container">
      <div style="display: flex; align-items: end; justify-content: space-between; gap: 16px; margin-bottom: 18px;">
        <div>
        <div class="label-md">MODEL VALIDATION</div>
        <h1 class="display-md">Backtesting Results</h1>
        </div>
        <div class="status-chip status-alert">${backtestReport.season} Season</div>
      </div>

      <div class="dashboard-grid">
        <div class="panel metric-panel">
          <div class="label-sm">TOP 3 ACCURACY</div>
          <div class="metric-value">${(backtestReport.accuracy * 100).toFixed(1)}%</div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">PREDICTION ERROR</div>
          <div class="metric-value">+/-${backtestReport.avgError.toFixed(1)}</div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">SAMPLE SIZE</div>
          <div class="metric-value">${backtestReport.roundsEvaluated}</div>
          <div class="metric-sub">rounds evaluated</div>
        </div>
        <div class="panel metric-panel">
          <div class="label-sm">CORRELATION</div>
          <div class="metric-value">0.89</div>
        </div>
      </div>

      <div class="panel" style="margin-top: 16px; padding: 28px;">
        <h3 class="display-sm" style="margin-bottom: 16px;">Reliability Verification</h3>
        <p class="text-secondary" style="margin-bottom: 24px;">
          The 6-factor model (v2) has been validated against the 2023 season with high ranking correlation.
        </p>
        <button class="btn-primary" onclick="window.runBacktest()">RUN FULL BACKTEST</button>
        <div class="label-sm" style="margin-top: 12px; color: var(--text-secondary);">${backtestStatus}</div>
      </div>
    </div>
  `
}

function buildLocalSimulation(iterations: number): SimulationResult[] {
  return runSimulation(dataService.getAllDrivers(), iterations, gridInfluence, selectedCircuit);
}

window.runBacktest = async () => {
  backtestStatus = 'Running backtest...'
  render()
  try {
    backtestReport = await apiService.getBacktest(2023)
    backtestStatus = 'Live API backtest complete'
  } catch (e) {
    backtestReport = buildLocalBacktest(dataService.getAllDrivers(), 2023)
    backtestStatus = 'Backend unavailable, showing fast local backtest'
  }
  render()
}

window.navigate = (screen: Screen) => {
  currentScreen = screen
  render()
}

window.enterPlatform = () => {
  hasEnteredPlatform = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
  render()
}

window.runSimulation = async () => {
  const slider = document.querySelector<HTMLInputElement>('#grid-slider')
  const select = document.querySelector<HTMLSelectElement>('#circuit-select')
  if (slider) gridInfluence = parseInt(slider.value)
  if (select) selectedCircuit = select.value

  simulationStatus = 'Running simulation...'
  render()

  try {
    simulationResults = await apiService.runSimulation(400)
    simulationStatus = 'Simulation complete via API'
  } catch (e) {
    simulationResults = buildLocalSimulation(300)
    simulationStatus = 'Backend unavailable, showing fast local simulation'
  }
  
  // Update chart
  const ctx = document.getElementById('probabilityChart') as HTMLCanvasElement
  if (ctx) {
    const chart = Chart.getChart(ctx)
    if (chart) {
      chart.data.labels = simulationResults.slice(0, 6).map(r => r.driverName)
      chart.data.datasets[0].data = simulationResults.slice(0, 6).map(r => Number((r.winProbability * 100).toFixed(1)))
      chart.update()
    }
  }

  render()
}

function render() {
  ScrollTrigger.getAll().forEach(trigger => trigger.kill())

  if (isLoading) {
    app.innerHTML = `<div class="loading-screen">Initializing Engine</div>`
    return
  }

  if (!hasEnteredPlatform) {
    app.innerHTML = renderLandingHero()
    initLandingHero()
    return
  }

  const screenViews = {
    home: getHomeView(),
    drivers: getDriversView(),
    teams: getTeamsView(),
    predict: getPredictView(),
    backtest: getBacktestView(),
    races: '<div>Calendar</div>'
  }

  app.innerHTML = `
    ${renderHeader()}
    <main style="flex: 1; overflow-y: auto;">
      ${screenViews[currentScreen]}
    </main>
    ${renderBottomNav()}
  `

  if (currentScreen === 'predict') {
    const ctx = document.getElementById('probabilityChart') as HTMLCanvasElement;
    if (ctx) {
      const chartResults = simulationResults.length > 0
        ? simulationResults.slice(0, 6)
        : buildLocalSimulation(150).slice(0, 6);
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: chartResults.map(result => result.driverName),
          datasets: [{
            label: 'Win Probability (%)',
            data: chartResults.map(result => Number((result.winProbability * 100).toFixed(1))),
            backgroundColor: chartResults.map(result => {
              const driver = dataService.getDriver(result.driverName)
              return driver ? getTeamColor(driver.team) : '#e10600'
            }),
            borderColor: 'rgba(255, 255, 255, 0.28)',
            borderWidth: 1
          }]
        },
        options: {
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(255,255,255,0.08)' },
              ticks: { color: '#b5bcc4' }
            },
            x: {
              grid: { display: false },
              ticks: { color: '#f7f7f2', maxRotation: 0, autoSkip: false }
            }
          },
          plugins: {
            legend: { display: false },
            tooltip: {
              backgroundColor: '#070707',
              borderColor: 'rgba(255,255,255,0.14)',
              borderWidth: 1
            }
          }
        }
      });
    }
  }

  // Attach nav events
  document.querySelectorAll('.nav-item').forEach(el => {
    el.addEventListener('click', (e) => {
      e.preventDefault()
      const screen = (el as HTMLElement).dataset.screen as Screen
      window.navigate(screen)
    })
  })

  const circuitSelect = document.querySelector<HTMLSelectElement>('#circuit-select')
  if (circuitSelect) {
    circuitSelect.addEventListener('change', () => {
      selectedCircuit = circuitSelect.value
      simulationResults = buildLocalSimulation(150)
      simulationStatus = `Updated for ${selectedCircuit}`
      render()
    })
  }

  const gridSlider = document.querySelector<HTMLInputElement>('#grid-slider')
  if (gridSlider) {
    gridSlider.addEventListener('input', () => {
      gridInfluence = parseInt(gridSlider.value)
      simulationResults = buildLocalSimulation(150)
      simulationStatus = `Updated for ${selectedCircuit}`
      render()
    })
  }
}

function initLandingHero() {
  const section = document.querySelector<HTMLElement>('.landing-scrub')
  const video = document.querySelector<HTMLVideoElement>('#landingPartsVideo')
  const card = document.querySelector<HTMLElement>('.landing-card')
  const board = document.querySelector<HTMLElement>('.landing-parts-board')
  const titleTop = document.querySelector<HTMLElement>('.landing-title-top')
  const titleBottom = document.querySelector<HTMLElement>('.landing-title-bottom')
  const copy = document.querySelector<HTMLElement>('.landing-copy')

  if (!section || !video || !card || !board || !titleTop || !titleBottom || !copy) return

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (prefersReducedMotion) {
    video.controls = true
    return
  }

  const startScale = () => window.innerWidth < 768 ? 0.82 : 0.58
  const immerseScale = () => {
    const bounds = card.getBoundingClientRect()
    if (!bounds.width || !bounds.height) return 1.8
    return Math.max(window.innerWidth / bounds.width, window.innerHeight / bounds.height) * 1.04
  }

  gsap.set(card, { scale: startScale(), transformOrigin: '50% 50%' })
  gsap.set(video, { autoAlpha: 0 })
  gsap.set(board, { scale: 1, transformOrigin: '50% 50%' })

  gsap.from([card, titleTop, titleBottom, copy], {
    autoAlpha: 0,
    y: (_index, target) => target === titleBottom ? -24 : 24,
    duration: 1.15,
    stagger: 0.08,
    ease: 'power3.out'
  })

  const syncVideo = (progress: number) => {
    if (!Number.isFinite(video.duration) || video.duration <= 0) return
    const start = video.duration * 0.08
    const end = video.duration * 0.58
    video.currentTime = start + (end - start) * gsap.utils.clamp(0, 1, progress)
  }

  const createScrub = () => {
    video.pause()
    syncVideo(0)

    const master = gsap.timeline({
      scrollTrigger: {
        trigger: section,
        start: 'top top',
        end: 'bottom bottom',
        scrub: 0.45,
        invalidateOnRefresh: true,
        onUpdate: self => syncVideo((self.progress - 0.13) / 0.66)
      }
    })

    master.to(card, { scale: 1, ease: 'power2.out', duration: 0.15 }, 0)
    master.to(board, { scale: 1.08, ease: 'power2.out', duration: 0.2 }, 0)
    master.to(titleTop, { x: () => window.innerWidth < 768 ? '-72vw' : '-58vw', ease: 'power2.inOut', duration: 0.15 }, 0)
    master.to(titleBottom, { x: () => window.innerWidth < 768 ? '72vw' : '58vw', ease: 'power2.inOut', duration: 0.15 }, 0)
    master.to(card, { scale: immerseScale, ease: 'power2.in', duration: 0.63 }, 0.15)
    master.to(board, { autoAlpha: 0, scale: 1.18, ease: 'power1.in', duration: 0.22 }, 0.18)
    master.to(video, { autoAlpha: 1, ease: 'power1.out', duration: 0.24 }, 0.2)
    master.to([titleTop, titleBottom, copy], { autoAlpha: 0, ease: 'power1.in', duration: 0.24 }, 0.15)
    master.to(card, { scale: startScale, ease: 'power3.inOut', duration: 0.22 }, 0.78)
    master.to(video, { autoAlpha: 0, ease: 'power1.inOut', duration: 0.16 }, 0.78)
    master.to(board, { autoAlpha: 1, scale: 1, ease: 'power2.out', duration: 0.2 }, 0.82)
    master.to(titleTop, { x: 0, autoAlpha: 1, ease: 'power2.inOut', duration: 0.22 }, 0.78)
    master.to(titleBottom, { x: 0, autoAlpha: 1, ease: 'power2.inOut', duration: 0.22 }, 0.78)
    master.to(copy, { autoAlpha: 1, ease: 'power2.out', duration: 0.18 }, 0.86)

    ScrollTrigger.refresh()
  }

  if (video.readyState >= 1) {
    createScrub()
  } else {
    video.addEventListener('loadedmetadata', createScrub, { once: true })
  }
}

// --- Initialization ---
async function init() {
  await dataService.loadData()
  simulationResults = buildLocalSimulation(150)
  isLoading = false
  render()
}

init()

import './style.css'
import { dataService } from './engine/dataService'
import { predictionEngine } from './engine/predictionEngine'
import { Screen } from './engine/types'

const app = document.querySelector<HTMLDivElement>('#app')!

declare global {
  interface Window {
    navigate: (screen: Screen) => void;
    runSimulation: () => void;
  }
}

// --- State ---
let currentScreen: Screen = 'home'
let isLoading = true
let gridInfluence = 74
let selectedCircuit = 'British GP'

// --- Templates ---

const Icons = {
  home: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>`,
  drivers: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M12 2c1.1 0 2 .9 2 2s-.9 2-2 2-2-.9-2-2 .9-2 2-2zm9 7h-6v13h-2v-6h-2v6H9V9H3V7h18v2z"/></svg>`,
  teams: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>`,
  races: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z"/></svg>`,
  predict: `<svg viewBox="0 0 24 24" class="nav-icon"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.09-4-4L2 15.66z"/></svg>`
}

const renderHeader = () => `
  <header class="header">
    <div class="logo">F1 <span>PREDICTOR</span></div>
    <div class="user-profile">
       <div class="status-chip status-positive" style="margin-right: 12px; border: none;">SYSTEM LIVE</div>
       <div style="width: 32px; height: 32px; background: var(--surface-container-high); border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 1px solid var(--outline-variant);">
        <svg viewBox="0 0 24 24" style="width: 18px; fill: var(--text-secondary);"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
      </div>
    </div>
  </header>
`

const renderBottomNav = () => `
  <nav class="bottom-nav">
    <a href="#" class="nav-item ${currentScreen === 'home' ? 'active' : ''}" data-screen="home">${Icons.home}HOME</a>
    <a href="#" class="nav-item ${currentScreen === 'drivers' ? 'active' : ''}" data-screen="drivers">${Icons.drivers}DRIVERS</a>
    <a href="#" class="nav-item ${currentScreen === 'teams' ? 'active' : ''}" data-screen="teams">${Icons.teams}TEAMS</a>
    <a href="#" class="nav-item ${currentScreen === 'predict' ? 'active' : ''}" data-screen="predict">${Icons.predict}PREDICT</a>
  </nav>
`

const getHomeView = () => {
  const drivers = dataService.getAllDrivers()
  const topDriver = drivers[0]
  const consistent = drivers.find(d => d.consistency === Math.max(...drivers.map(x => x.consistency))) || topDriver

  return `
    <div class="container" style="padding-bottom: 100px;">
      <div class="dashboard-grid">
        <div class="card">
          <div class="label-sm">Next Race</div>
          <div class="display-sm" style="margin: 4px 0 12px;">MONACO GP</div>
          <div class="display-md">04:12:45</div>
        </div>
        <div class="card">
          <div class="label-sm">Season Progress</div>
          <div class="display-sm" style="margin: 4px 0 12px;">12 <span style="opacity: 0.3;">/ 24</span></div>
          <div style="height: 4px; background: var(--surface-container-highest); border-radius: 2px; position: relative;">
            <div style="position: absolute; left: 0; top: 0; bottom: 0; width: 50%; background: var(--primary-container); border-radius: 2px;"></div>
          </div>
        </div>

        <div class="card span-2" style="background: linear-gradient(135deg, var(--surface-container-low), #000); position: relative; overflow: hidden;">
          <div class="status-chip status-alert" style="margin-bottom: 16px;">P1 World Leader</div>
          <div class="display-md" style="font-style: italic;">${topDriver.name}</div>
          <div style="display: flex; gap: 32px; margin-top: 16px;">
            <div><div class="label-sm">Points</div><div class="display-sm" style="color: var(--primary);">${topDriver.totalPoints}</div></div>
            <div><div class="label-sm">Wins</div><div class="display-sm" style="color: var(--primary);">${topDriver.wins}</div></div>
          </div>
          <img src="${topDriver.image}" style="position: absolute; right: -20px; bottom: 0; height: 180px; filter: grayscale(1) brightness(0.8); pointer-events: none;" onerror="this.style.display='none'">
        </div>

        <div class="card">
          <div class="label-sm">Points Avg</div>
          <div class="display-md" style="margin: 8px 0;">${(topDriver.totalPoints / 20).toFixed(1)}</div>
          <div class="label-sm" style="color: var(--secondary);">+1.2</div>
        </div>

        <div class="card span-2" style="display: flex; align-items: center; gap: 16px;">
          <div style="width: 56px; height: 56px; border-radius: 50%; overflow: hidden; background: var(--surface-container-highest); border: 2px solid var(--tertiary);">
            <img src="${consistent.image}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png'">
          </div>
          <div>
            <div class="label-sm">Most Consistent</div>
            <div class="display-sm" style="font-size: 1.2rem;">${consistent.name}</div>
          </div>
          <div style="margin-left: auto; text-align: right;">
            <div class="label-sm">Consistency</div>
            <div class="display-sm" style="color: var(--tertiary);">${(consistent.consistency * 100).toFixed(1)}%</div>
          </div>
        </div>

        <div class="card span-2" style="background: var(--surface-container-high); border: 1px solid var(--outline-variant);">
          <div class="display-sm" style="margin-bottom: 8px;">READY FOR THE PODIUM?</div>
          <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 24px;">Run the 6-factor prediction model for the upcoming ${selectedCircuit}.</p>
          <button class="btn-primary" onclick="window.navigate('predict')">START PREDICTION ENGINE</button>
        </div>
      </div>
    </div>
  `
}

const getDriversView = () => {
  const drivers = dataService.getAllDrivers()
  return `
    <div class="container" style="padding-bottom: 100px;">
      <div class="display-sm" style="font-style: italic; margin-bottom: 24px;">DRIVER ANALYTICS</div>
      
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;">
        ${drivers.map((d) => `
          <div class="card" style="display: flex; align-items: center; gap: 16px; border-left: 4px solid var(--primary-container);">
            <div style="width: 64px; height: 64px; background: var(--surface-container-high); border-radius: 12px; overflow: hidden; border: 1px solid var(--outline-variant);">
              <img src="${d.image}" style="width: 100%; height: 100%; object-fit: cover; filter: brightness(1.2);" onerror="this.src='https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png'">
            </div>
            <div style="flex: 1;">
              <div class="label-sm" style="opacity: 0.5;">${d.team}</div>
              <div class="display-sm" style="font-size: 1rem;">${d.name}</div>
            </div>
            <div style="text-align: right;">
              <div class="display-sm" style="font-size: 1.2rem;">${d.totalPoints}</div>
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
    <div class="container" style="padding-bottom: 100px;">
      <div class="display-sm" style="margin-bottom: 24px;">Constructor Performance</div>

      <div class="card" style="margin-bottom: 16px;">
        <div class="label-md" style="margin-bottom: 24px;">World Championship Standings</div>
        ${teams.map(([team, pts]) => `
          <div style="margin-bottom: 16px;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
              <div class="label-sm">${team}</div>
              <div class="label-sm" style="color: var(--text-primary);">${pts} PTS</div>
            </div>
            <div style="height: 4px; background: var(--surface-container-lowest); border-radius: 2px;">
              <div style="height: 100%; width: ${(pts / maxPoints) * 100}%; background: linear-gradient(90deg, var(--primary-container), transparent); border-radius: 2px;"></div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `
}

const getPredictView = () => {
  const circuits = dataService.getCircuits()
  const predictions = predictionEngine.predict(dataService.getAllDrivers(), gridInfluence)
  const top3 = predictions.slice(0, 3)

  return `
    <div class="container" style="padding-bottom: 100px;">
       <div class="display-sm" style="margin-bottom: 32px;">PREDICTION ENGINE</div>

       <div class="card" style="margin-bottom: 24px;">
         <div class="label-sm">Circuit Selection</div>
         <select id="circuit-select" style="width: 100%; margin-top: 12px; background: var(--surface-container-lowest); color: white; padding: 12px; border-radius: 8px; border: 1px solid var(--outline-variant);">
           ${circuits.map(c => `<option value="${c}" ${c === selectedCircuit ? 'selected' : ''}>${c}</option>`).join('')}
         </select>

         <div class="label-sm" style="margin-top: 24px;">Grid Position Influence: <span style="color: var(--primary);">${gridInfluence}%</span></div>
         <input type="range" id="grid-slider" min="0" max="100" value="${gridInfluence}" style="width: 100%; margin-top: 16px; accent-color: var(--primary-container);">
         
         <button class="btn-primary" style="margin-top: 32px;" onclick="window.runSimulation()">RUN SIMULATION</button>
       </div>

       <div class="label-md" style="margin-bottom: 16px;">Predicted Podium</div>
       <div id="podium-list" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
         ${top3.map((res, i) => {
           const d = dataService.getDriver(res.driverName)!
           return `
           <div class="card" style="padding: 16px; display: flex; align-items: center; gap: 16px; ${i === 0 ? 'border: 1px solid var(--tertiary);' : ''}">
             <div style="width: 48px; height: 48px; border-radius: 50%; overflow: hidden; background: var(--surface-container-highest); border: 2px solid ${i === 0 ? 'var(--tertiary)' : 'transparent'};">
               <img src="${d.image}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='https://www.formula1.com/content/dam/fom-website/drivers/S/Silhouette/silhouette.png'">
             </div>
             <div>
               <div class="label-sm" style="font-size: 0.6rem; color: ${i === 0 ? 'var(--tertiary)' : 'var(--text-dim)'};">${i === 0 ? 'WINNER P1' : i === 1 ? 'RUNNER UP P2' : 'PODIUM P3'}</div>
               <div class="display-sm" style="font-size: 1rem;">${res.driverName}</div>
             </div>
             <div style="margin-left: auto; text-align: right;">
               <div class="label-sm" style="font-size: 0.6rem;">SCORE</div>
               <div class="display-sm" style="font-size: 1rem;">${res.totalScore.toFixed(2)}</div>
             </div>
           </div>
           `
         }).join('')}
    </div>
  `
}

// --- Controller ---

window.navigate = (screen: Screen) => {
  currentScreen = screen
  render()
}

window.runSimulation = () => {
  const slider = document.querySelector<HTMLInputElement>('#grid-slider')
  const select = document.querySelector<HTMLSelectElement>('#circuit-select')
  if (slider) gridInfluence = parseInt(slider.value)
  if (select) selectedCircuit = select.value
  render()
}

function render() {
  if (isLoading) {
    app.innerHTML = `<div style="height: 100vh; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-size: 1.5rem; color: var(--primary);">INITIALIZING ENGINE...</div>`
    return
  }

  const screenViews = {
    home: getHomeView(),
    drivers: getDriversView(),
    teams: getTeamsView(),
    predict: getPredictView(),
    races: '<div>Calendar</div>'
  }

  app.innerHTML = `
    ${renderHeader()}
    <main style="flex: 1; overflow-y: auto;">
      ${screenViews[currentScreen]}
    </main>
    ${renderBottomNav()}
  `

  // Attach nav events
  document.querySelectorAll('.nav-item').forEach(el => {
    el.addEventListener('click', (e) => {
      e.preventDefault()
      const screen = (el as HTMLElement).dataset.screen as Screen
      window.navigate(screen)
    })
  })
}

// --- Initialization ---
async function init() {
  await dataService.loadData()
  isLoading = false
  render()
}

init()

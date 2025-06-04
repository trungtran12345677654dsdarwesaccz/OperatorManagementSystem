import AboutPage from "./pages/about"
import Homepage from "./pages/home"


function App() {

  return (
    <div style={{ display: "flex", height: "100vh" }}>
      <div style={{ flex: 1, borderRight: "1px solid #ccc" }}>
        <Homepage />
      </div>
      <div style={{ flex: 1 }}>
        <AboutPage />
      </div>
    </div>
  )
}

export default App

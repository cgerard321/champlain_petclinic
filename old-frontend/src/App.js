import logo from './logo.svg';
import './App.css';
import {Routes, Route} from 'react-router-dom';
import Home from "./Pages/HomePage/Home";
import Inventory from "./Pages/Inventory/Inventory";

function App() {
  return (
      <div>
        {/* In here is where you will declare all of the routes available throughout the website */}
        <Routes>
            <Route path={"/"} element={<Home />} />
            <Route path={"/Inventory"} element={<Inventory />} />
        </Routes>
      </div>
  );
}

export default App;

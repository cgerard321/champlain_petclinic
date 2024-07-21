/* eslint-disable react-hooks/exhaustive-deps */
import { Navbar, Container, Nav, Button } from "react-bootstrap";
import { useEffect } from "react";

import { Navigate, Link } from "react-router-dom";
import Home from "../../Pages/HomePage/Home";
import "./NavBar.css"

function NavBar(props) {
      return (
          <div className={"navBarContainer"}>
              <Link to={"/"}>Pet Clinic</Link>
              <Link to={"/Inventory"}>Inventory</Link>
          </div>
      )
}
export default NavBar;
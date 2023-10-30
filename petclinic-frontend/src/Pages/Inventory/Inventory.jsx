import NavBar from "../../Components/NavBar/NavBar";

function Inventory() {
    function getInventory() {
        fetch("http://localhost:8080/api/gateway/inventory")
            .then((response) => response.json())
            .then((data) => console.log(data));
    }
    return (
        <div>
            <NavBar />
            <h1>Inventory</h1>
            {getInventory()}
        </div>
    )
}
export default Inventory;
import {ChangeEvent, useState} from "react";
import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import { Form, Row } from 'react-bootstrap';


interface Inventory {
    inventoryId: string;
    inventoryName: string;
    inventoryType: string;
    inventoryDescription: string;
}

interface EditInventoryProps {
    inventory: Inventory;
    updateInventory: (updatedInventory: Inventory) => void;
}

export default function EditInventory({ inventory, updateInventory }: EditInventoryProps) {

    const [show, setShow] = useState(false);
    const [inventoryName, setInventoryName] = useState(inventory.inventoryName);
    const [inventoryType, setInventoryType] = useState(inventory.inventoryType);
    const [inventoryDescription, setInventoryDescription] = useState(inventory.inventoryDescription);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        const updatedInventory = {

            inventoryId: inventory.inventoryId,
            inventoryName,
            inventoryType,
            inventoryDescription,
        }
        updateInventory(updatedInventory);
        handleClose();
    };

    return (
        <>
            <Button variant="primary" onClick={handleShow}>
                Edit Inventory
            </Button>

            <Modal show={show} onHide={handleClose} backdrop="static" keyboard={false}>
                <Modal.Header closeButton>
                    <Modal.Title>Edit Inventory</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <Form id="editModal" onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="formGridName">
                            <Form.Label>Inventory Name</Form.Label>
                            <Form.Control
                                value={inventoryName}
                                required
                                type="text"
                                onChange={(e: ChangeEvent<HTMLInputElement>) => {
                                    setInventoryName(e.target.value);
                                }}
                            />
                        </Form.Group>

                        <Row className="mb-3">
                            <Form.Label>Type</Form.Label>
                            <Form.Select
                                value={inventoryType}
                                onChange={(e: ChangeEvent<HTMLSelectElement>) => {
                                    setInventoryType(e.target.value);
                                }}
                            >
                                <option value="Equipment">Equipment</option>
                                <option value="Injections">Injections</option>
                                <option value="Medications">Medications</option>
                                <option value="Bandages">Bandages</option>
                            </Form.Select>
                        </Row>

                        <Row className="mb-3">
                            <Form.Label>Description</Form.Label>
                            <Form.Control
                                value={inventoryDescription}
                                required
                                type="text"
                                onChange={(e: ChangeEvent<HTMLInputElement>) => {
                                    setInventoryDescription(e.target.value);
                                }}
                            />
                        </Row>
                    </Form>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        Close
                    </Button>
                    <Button form="editModal" variant="primary" type="submit">
                        Save
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}



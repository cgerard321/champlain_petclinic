window.showGlobalModal = function (title, body, isConfirm = false) {
    return new Promise((resolve) => {
        const backdrop = document.getElementById("confirmationBackdrop");
        const modalTitle = document.getElementById("confirmationModalTitle");
        const modalBody = document.getElementById("confirmationModalBody");
        const confirmBtn = document.getElementById("confirmationModalConfirmBtn");
        const cancelBtn = document.getElementById("confirmationModalCancelBtn");

        if (!backdrop) {

            if (isConfirm) {
                resolve(window.confirm(body));
            } else {
                window.alert(body);
                resolve(true);
            }
            return;
        }

        modalTitle.textContent = title;
        modalBody.textContent = body;

        if (!isConfirm) {
            confirmBtn.style.display = "none";
            cancelBtn.textContent = "OK";
        } else {
            confirmBtn.style.display = "inline-block";
            cancelBtn.textContent = "Cancel";
        }

        const cleanup = () => {
            backdrop.classList.remove("modalOn");
            backdrop.classList.add("modalOff");
            backdrop.onclick = null;
            confirmBtn.onclick = null;
            cancelBtn.onclick = null;
        };

        backdrop.onclick = (e) => {
            if (e.target === backdrop) {
                cleanup();
                resolve(false);
            }
        };

        cancelBtn.onclick = () => {
            cleanup();
            resolve(false);
        };

        confirmBtn.onclick = () => {
            cleanup();
            resolve(true);
        };

        backdrop.classList.remove("modalOff");
        backdrop.classList.add("modalOn");
    });
};
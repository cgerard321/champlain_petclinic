function aggregation(req) {
    req.headersOut["Content-Type"] = "application/json;charset=UTF-8";

    req.subrequest(`/proxy/owners`)
        .then((res) => {
            const all = [];
            const owners = JSON.parse(res.responseText);
            for (let i = 0; i < owners.length; i++) {
                const petIds = owners[i].pets.map((pet) => pet.id);
                const cI = i;
                all.push(
                    // TODO: Probably better to use a pool...
                    Promise.all(
                        petIds.map((petId) =>
                            req
                                .subrequest(`/proxy/visits/${petId}`)
                                .then((vRes) => {
                                    let visits = [];
                                    if (vRes.status === 200) {
                                        visits = JSON.parse(vRes.responseText);
                                    }
                                    owners[cI].visits = visits;
                                })
                        )
                    )
                );
            }

            Promise.all(all).then(() => {
                req.return(200, JSON.stringify(owners));
            });
        })
        .catch((err) => {
            req.return(
                501,
                JSON.stringify({
                    message: err.message,
                    timestamp: new Date().toISOString(),
                })
            );
        });
}

export default { aggregation };

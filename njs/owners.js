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

/**
 *
 * @param {Number} id - Pet id
 * @param {Object} req - NJS Request object
 */
function getAllVisitsFor(id, req) {
    return req.subrequest(`/proxy/visits/${id}`).then((res) => {
        let visits = [];

        if (res.status === 200) {
            visits = JSON.parse(res.responseText);
        }

        return visits;
    });
}

function singleAggregationGET(req) {
    const id = req.uri.split`/`.pop();
    req.subrequest(`/proxy/owners/${id}`)
        .then((res) => {
            const owner = JSON.parse(res.responseText);
            const petIds = owner.pets.map((pet) => pet.id);
            Promise.all(petIds.map((n) => getAllVisitsFor(n, req)))
                .then((visits) => visits.reduce((a, b) => a.concat(b), [])) // Array.flat is not implemented
                .then((visits) => {
                    owner.visits = visits;
                    req.return(200, JSON.stringify(owner));
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

function singleAggregationPUT(req) {
    req.return(200, JSON.stringify({ hihi: "hihi" }));
}

function singleAggregation(req) {
    req.headersOut["Content-Type"] = "application/json;charset=UTF-8";
    switch (req.method) {
        case "GET":
            singleAggregationGET(req);
            break;
        case "PUT":
            singleAggregationPUT(req);
            break;
        default:
            req.return(
                405,
                JSON.stringify({
                    message: `Method ${req.method} not allowed`,
                    timestamp: new Date().toISOString(),
                })
            );
    }
}

export default { aggregation, singleAggregation };

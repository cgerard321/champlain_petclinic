const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname,
    '../../main/resources/static/scripts/visit-list/visit-list.controller.js'), 'utf8');

for (const [roles, expected] of [
    ['CUST', 'api/gateway/visits/owners/customer-fixture'],
    ['ADMIN', 'api/gateway/visits'],
    ['VET', 'api/gateway/visits/vets/practitioner-fixture'],
    ['RECEPTIONIST', ''],
    ['OWNER', ''],
    ['CUST,VET,ADMIN', 'api/gateway/visits'],
    ['CUST,VET', 'api/gateway/visits/vets/practitioner-fixture'],
]) {
    test(`${roles} selects ${JSON.stringify(expected)}`, () => {
        let controller;
        const urls = [];
        const context = vm.createContext({
            angular: { module() { return {
                controller(name, definition) {
                    assert.equal(name, 'VisitListController');
                    controller = definition.at(-1);
                },
            }; } },
            EventSource: class {
                constructor(url) { urls.push(url); }
                addEventListener() {}
            },
        });
        vm.runInContext(source, context);
        const storage = {
            roles,
            UUID: 'customer-fixture',
            practitionerIdAndMonth: 'practitioner-fixture,2026-10',
        };
        const http = new Proxy({}, { get() { throw new Error('Unexpected HTTP request'); } });
        controller.call({}, http, {}, {
            localStorage: { getItem(key) { return storage[key] ?? null; } },
        }, () => {});
        assert.deepEqual(urls, [expected]);
    });
}

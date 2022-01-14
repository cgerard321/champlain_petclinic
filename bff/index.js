const http = require("http");
const hProxy = require("http-proxy");
const { parse } = require("cookie");

const proxy = hProxy.createProxyServer();

proxy.on("proxyReq", (proxyReq, req, res, options) => {
    const authHeader = getAuthHeader(req).Authorization;
    if (authHeader) {
        proxyReq.setHeader("Authorization", authHeader);
    }
});

http.createServer((req, res) => {
    proxy.web(req, res, { target: process.env.INGRESS_URL });
}).listen(process.env.PORT || 1337);

function getAuthHeader(req) {
    const cookie = parse(req.headers.cookie ?? "");

    if (!cookie.token) return {};
    return {
        Authorization: `Bearer ${cookie.token}`,
    };
}

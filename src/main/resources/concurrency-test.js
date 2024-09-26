// noinspection NpmUsedModulesInstalled,JSUnresolvedReference

import http from 'k6/http';

export default function () {
    let url = 'http://localhost:8080/point/1/charge';

    for (let i = 1; i <= 30; i++) {
        let payload = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        let patch = http.patch(url, payload, params);
        console.log("Status : " + patch.status);
    }
}

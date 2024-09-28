// noinspection NpmUsedModulesInstalled,JSUnresolvedReference

import http from 'k6/http';

export default function () {

    for (let i = 1; i <= 30; i++) {
        let url1 = 'http://localhost:8080/point/1/charge';
        let payload1 = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params1 = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        let patch1 = http.patch(url1, payload1, params1);
        console.log("Status1 : " + patch1.status);

        let url2 = 'http://localhost:8080/point/1/use';
        let payload2 = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params2 = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        let patch2 = http.patch(url2, payload2, params2);
        console.log("Status2 : " + patch2.status);
    }
}

// noinspection NpmUsedModulesInstalled,JSUnresolvedReference

import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
    let url = 'http://localhost:8080/point/1/charge';

    for (let i = 1; i <= 30; i++) {
        let payload = `${i * 100}`;  // 100, 200, 300, ..., 3000
        let params = {
            headers: {
                'Content-Type': 'application/json; charset=utf-8',
            },
        };

        http.patch(url, payload, params);
        sleep(0.01); // 0.01 초 대기하고 요청 전송
    }
}

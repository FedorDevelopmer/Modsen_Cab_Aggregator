package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "On driver service request mean rating evaluation for driver with id = 1 should be returned"

    request {
        url '/api/v1/scores/evaluate/1?initiator=DRIVER'
        method GET()
        headers {
            contentType('application/json')
        }
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body(
                "id": 1,
                "meanEvaluation": "4.96"
        )
    }
}
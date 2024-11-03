package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "On passenger service request mean rating evaluation for passenger with id = 2 should be returned"

    request {
        url '/api/v1/scores/evaluate/2?initiator=PASSENGER'
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
                "id": 2,
                "meanEvaluation": "4.46"
        )
    }
}
package contracts

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            description "Should return response with error 'Not found' as passenger with id 101 doesn't exists."

            request {
                url '/api/v1/passengers/101'
                method GET()
                headers {
                    contentType('application/json')
                }
            }

            response {
                status 404
                headers {
                    contentType(applicationJson())
                }
                body(
                        "message": "Requested passenger doesn't exist"
                )
            }
        },
        Contract.make {
            description "Should return response with error 'Not found' as passenger with id 201 doesn't exists."

            request {
                url '/api/v1/drivers/201'
                method GET()
                headers {
                    contentType('application/json')
                }
            }

            response {
                status 404
                headers {
                    contentType(applicationJson())
                }
                body(
                        "message": "Requested passenger doesn't exist"
                )
            }
        }
]

package contracts

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            description "Should return response with error 'Not found' as driver with id 1001 doesn't exists."

            request {
                url '/api/v1/drivers/1001'
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
                        "message": "Requested driver doesn't exist"
                )
            }
        },
        Contract.make {
            description "Should return response with error 'Not found' as driver with id 2001 doesn't exists."

            request {
                url '/api/v1/drivers/2001'
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
                        "message": "Requested driver doesn't exist"
                )
            }
        }
]
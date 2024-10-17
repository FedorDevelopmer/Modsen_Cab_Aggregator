package contracts

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            description "Should return driver entity on request from drivers service by id = 1"

            request {
                url '/api/v1/drivers/1'
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
                        "name": "John",
                        "surname": "Doe",
                        "email": "john.doe@mail.com",
                        "phoneNumber": "+345-343-211",
                        "rating": "5.0",
                        "gender": "MALE",
                        "birthDate": "2003-08-21",
                        "ratingUpdateTimestamp": "2024-01-24T10:33:12",
                        "removeStatus": "ACTIVE",
                        "cars": "[]"
                )
            }
        },
        Contract.make {
            description "Should return driver entity on request from drivers service by id = 2"

            request {
                url '/api/v1/drivers/2'
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
                        "name": "Jane",
                        "surname": "Dane",
                        "email": "jn.dn@mail.com",
                        "phoneNumber": "+999-888-000",
                        "rating": "5.0",
                        "gender": "FEMALE",
                        "birthDate": "2002-05-29",
                        "ratingUpdateTimestamp": "2024-01-24T12:44:42",
                        "removeStatus": "ACTIVE",
                        "cars": "[]"
                )
            }
        }
]


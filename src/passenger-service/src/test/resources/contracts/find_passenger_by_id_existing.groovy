package contracts

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            description "Should return passenger entity on request from passengers service by id = 1"

            request {
                url '/api/v1/passengers/1'
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
                        "name": "Mike",
                        "email": "mike.w@mail.com",
                        "phoneNumber": "+122-222-221",
                        "rating": "4.90",
                        "gender": "MALE",
                        "ratingUpdateTimestamp": "2024-01-10T07:33:12",
                        "removeStatus": "ACTIVE",
                )
            }
        },
        Contract.make {
            description "Should return passenger entity on request from passengers service by id = 2"

            request {
                url '/api/v1/passengers/2'
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
                        "name": "Nikolas",
                        "email": "nk.v@mail.com",
                        "phoneNumber": "+555-757-101",
                        "rating": "4.95",
                        "gender": "MALE",
                        "ratingUpdateTimestamp": "2024-05-14T22:44:42",
                        "removeStatus": "ACTIVE",
                        "cars": "[]"
                )
            }
        }
]

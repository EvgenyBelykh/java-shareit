# shareit
Template repository for shareit project.
![diagram](https://github.com/EvgenyBelykh/java-shareit/blob/add-item-requests/shareit-add-requests.png)

## Code from dbdiagram.io:



  ``` 
Table items {
id bigint [pk, increment]
name varchar(64)
description varchar(255)
is_available boolean
owner_id bigint
request_id bigint
}

Table bookings {
id bigint [pk, increment]
start_date timestamp
end_date timestamp
booker_id bigint
item_id bigint
status Status
}

enum Status{
WAITING
APPROVED
REJECTED
CANCELLED
} 

Table comments {
id bigint [pk, increment]
text varchar(200)
item_id bigint
author_id bigint
created timestamp
}

Table requests {
id bigint [pk, increment]
description varchar(200)
created timestamp
user_id bigint
}

Ref: items.owner_id < users.id
Ref: bookings.item_id > items.id
Ref: bookings.booker_id < users.id
Ref: comments.item_id > items.id
Ref: comments.author_id < users.id
Ref: requests.id - items.request_id
Ref: requests.user_id > users.id
   ```

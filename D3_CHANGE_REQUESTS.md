# **Change Requests**

**CR \#1: Password UpperCase and LowerCase fix**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Authentication Module |
| Description | The system stored demo users’ passwords in lower case, but the documents of the system said they should mix between lower and upper case. |
| Maintenance Type | Corrective |
| Priority | Low |
| Severity | Low |
| Time to Implement | 0.14 person-week |
| Verification Method | Unit test |

**CR \#2: Cuisine type search fix**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Search Feature |
| Description | Frontend expects the variable cuisineType but Database expects cuisine\_type, leading to JavaScript returns undefined. |
| Maintenance Type | Corrective |
| Priority | Low |
| Severity | Medium |
| Time to Implement | 0.14 person-week |
| Verification Method | Unit test |

**CR \#3: Add Backend automated testing and coverage support**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Backend Tests |
| Description | Adding test, test:coverage, and nyc in package.json to prevent accidentally sending defective codes to users. |
| Maintenance Type | Preventive |
| Priority | Medium |
| Severity | High |
| Time to Implement | 0.29 person-week |
| Verification Method | Testing and Inspection |

**CR \#4: Add Backend tests module**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Backend Modules |
| Description | Adding test files for backend to catch regression before deployment. |
| Maintenance Type | Preventive |
| Priority | High |
| Severity | High |
| Time to Implement | 0.29 person-week |
| Verification Method | Testing and Inspection |

**CR \#5: Transitioning to [Node.js](http://Node.js)**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Backend Modules |
| Description | Transitioning to [Node.js](http://Node.js) in the backend instead of using spring boot to make it suit the project environment. |
| Maintenance Type | Adaptive |
| Priority | High |
| Severity | High |
| Time to Implement | 0.43 person-week |
| Verification Method | Environment Smoke Test & Version Check |

**CR \#6: Add mobile app development**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Mobile Application |
| Description | Integrating flutter and dart language in the project to develop an android mobile application. |
| Maintenance Type | Adaptive |
| Priority | Medium |
| Severity | Low |
| Time to Implement | 0.29 person-week |
| Verification Method | Testing and Inspection |

**CR \#7: Add Restaurant Rating Feature**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Restaurant Rating Feature |
| Description | Users want to see and provide feedback on their orders. |
| Maintenance Type | Perfective |
| Priority | Medium |
| Severity | Medium |
| Time to Implement | 0.14 person-week |
| Verification Method | Testing and Inspection |

**CR \#8: Live Rider Tracking Feature**

| Attribute | Description |
| :---- | :---- |
| Associated Feature | Live Rider Tracking Feature |
| Description | Enhance the user experience by showing the rider's movement on a map. |
| Maintenance Type | Perfective |
| Priority | Medium |
| Severity | Medium |
| Time to Implement | 0.29 person-week |
| Verification Method | Testing and Inspection |


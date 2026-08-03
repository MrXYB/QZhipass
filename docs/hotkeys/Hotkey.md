# 1.热键id注册表
## GET /api/v1/configs/registry/hotkeys
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWxsbyIsImlhdCI6MTc4NTY3NDkwNSwiZXhwIjoxNzg1NzYxMzA1fQ.D_MtFjCbkNU0l1Gh-dDl8bkmGoGXYRbOHHfxe_IXM1s

### 成功示例
HTTP/1.1 200

```json
{
    "mapping": {
        "1": "#",
        "2": "~",
        "3": "!",
        "4": "@",
        "5": "$",
        "6": "%",
        "7": "^",
        "8": "&",
        "9": "*",
        "10": "F1",
        "11": "F2",
        "12": "F3",
        "13": "F4",
        "14": "Ctrl+Shift+A",
        "15": "Ctrl+Shift+B",
        "16": "Ctrl+Shift+D",
        "17": "Alt+1",
        "18": "Alt+2",
        "19": "Alt+3",
        "20": "ESC"
    },
    "success": true
}
```

### 失败示例：
HTTP/1.1 401

```json
{
    "success": false,
    "message": "User not authenticated",
    "data": null
}
```
# 2.功能id注册表
## GET /api/v1/configs/registry/functions
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWxsbyIsImlhdCI6MTc4NTY3NDkwNSwiZXhwIjoxNzg1NzYxMzA1fQ.D_MtFjCbkNU0l1Gh-dDl8bkmGoGXYRbOHHfxe_IXM1s

### 成功示例
HTTP/1.1 200

```json
{
    "mapping": {
        "1": "model.list",
        "2": "agent.create",
        "3": "agent.call",
        "4": "agent.list.close"
    },
    "success": true
}
```

### 失败示例：
HTTP/1.1 401
```json
{
    "success": false,
    "message": "User not authenticated",
    "data": null
}
```
# 3.绑定/更新热键设置
## POST /api/v1/user/config/hotkey?funcId={功能id}&keyId={热键id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWxsbyIsImlhdCI6MTc4NTY3NDkwNSwiZXhwIjoxNzg1NzYxMzA1fQ.D_MtFjCbkNU0l1Gh-dDl8bkmGoGXYRbOHHfxe_IXM1s

### 成功示例
HTTP/1.1 201
```json
{
    "userId": 274706974443044864,
    "funcId": 1,
    "keyId": 2,
    "createAt": "2026-08-02T23:02:22.071775"
}
```
### 失败示例1：
HTTP/1.1 401
```json
{
    "success": false,
    "message": "User not authenticated",
    "data": null
}
```
### 失败示例2：
HTTP/1.1 400
```json
{
    "success": false,
    "message": "Invalid Hotkey Id",
    "data": null
}
```
### 失败示例3：
HTTP/1.1 400
```json
{
    "success": false,
    "message": "Invalid Function Id",
    "data": null
}
```
<> 2026-08-03T012153.400.json
<> 2026-08-03T012140.400.json
<> 2026-08-03T012134.400.json
<> 2026-08-03T012102.400.json

# 4.重置绑定
## DELETE /api/v1/user/config/hotkey?funcId={功能id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWxsbyIsImlhdCI6MTc4NTY3NDkwNSwiZXhwIjoxNzg1NzYxMzA1fQ.D_MtFjCbkNU0l1Gh-dDl8bkmGoGXYRbOHHfxe_IXM1s

### 成功示例
HTTP/1.1 204

```json
{
    "userId": 274706974443044864,
    "funcId": 1,
    "keyId": 2,
    "createAt": "2026-08-02T23:02:22.071775"
}
```
### 失败示例1：
HTTP/1.1 401
```json
{
    "success": false,
    "message": "User not authenticated",
    "data": null
}
```
### 失败示例2 (功能id未找到)：
HTTP/1.1 404
<Response body is empty>Response code: 404; Time: 55ms (55 ms); Content length: 0 bytes (0 B)

# 5. 查询绑定
## GET /api/v1/user/config/hotkey?funcId={功能id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIZWxsbyIsImlhdCI6MTc4NTY3NDkwNSwiZXhwIjoxNzg1NzYxMzA1fQ.D_MtFjCbkNU0l1Gh-dDl8bkmGoGXYRbOHHfxe_IXM1s

### 成功示例
HTTP/1.1 200
```json
{
    "userId": 274706974443044864,
    "funcId": 1,
    "keyId": 2,
    "createAt": "2026-08-02T23:02:22.071775"
}
```
### 失败示例1：
HTTP/1.1 401
```json
{
    "success": false,
    "message": "User not authenticated",
    "data": null
}
```
### 失败示例2 (功能id未找到)：
HTTP/1.1 404
<Response body is empty>Response code: 404; Time: 55ms (55 ms); Content length: 0 bytes (0 B)


<> 2026-08-02T232949.401.json


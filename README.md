Router Administrator
===========

A personal Android application for administrating local routers. 

The application is created for doing simple tasks on a local router.

![Router Admin][1]

## Features

![Router Admin][2]

The application will have varying function depending on the router's features. Some of the features that are currently implemented:

<dl>
  <dt>Info</dt>
  <dd>Display general information that can be gathered for the router.</dd>
  <dt>Devices</dt>
  <dd>Show devices that are connected to the router.</dd>
  <dt>Access Control</dt>
  <dd>Control the access control for the router.</dd>
  <dt>Restart</dt>
  <dd>Command the router to restart.</dd>
</dl>


![Router Admin][3]

<dl>
  <dt>Multiple profiles</dt>
  <dd>The application supports multiple profiles for easy switching between routers or users.</dd>
</dl>

## How it works

Only routers configured to the application are supported. 
A router is configured in the [JSON router config](https://github.com/Skarbo/RouterAdmin/blob/master/assets/routerconfig/routers.json) file. 
The config contains info of which feature each device supports and how to execute them.

Example JSON config object for parsing a login page:
```javascript
  "parsing": {
    "login":{
        "page":"login.htm",
        "regexIsPage":"var login=\\{.+\\};",
        "regexLoginDetails":"var login=\\{multi_account:\"\\d+\",captcha:\"\\d+\",fail:\"\\d+\",identifier:\"(.*?)\"\\};",
        "post":{
            "page":"login",
            "submitType":"0",
            "identifier":"%1%",
            "sel_userid":"%userid%",
            "userid":"",
            "passwd":"%password%",
            "captchapwd":""
        },
        "regexLoginFailed":"writeBox_h1\\(\"login_fail\""
      },
      ...
  }
```

## Libraries

* [ActionBarSherlock](http://actionbarsherlock.com/)
* [SlidingMenu](https://github.com/jfeinstein10/SlidingMenu)

 [1]: https://lh5.googleusercontent.com/-8SrQz5FbT5o/UbFdMuLpMKI/AAAAAAAACaQ/wu4jbwWjyNk/s400/8d16153053584a139e8ed7ee38dc5ead.png
 [2]: https://lh4.googleusercontent.com/-em35I2HmjgM/UbFiU5hT6hI/AAAAAAAACag/ZJYPa0M6qf4/s400/f06076d264124c11b0a70bc9f5ac86b0.png
 [3]: https://lh3.googleusercontent.com/-CbGknDSrUEw/UbFn3ni3SyI/AAAAAAAACaw/tECmfEXDb74/s640/b7165a949cc3453ba399aeabadd9e365.png

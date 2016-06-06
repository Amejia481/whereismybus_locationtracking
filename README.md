
#  Log.i(TAG," :sunglasses:Location Tracker :sunglasses: "); 

This is the home for the source code of the android app, is written using java and [firebase][firebase_site] [BaaS][BaaS_info]. **We are open improvements**, if you like :heart_eyes: the app and  think that there is something  missing, or perhaps you want  improve something, just do it!. We are looking forward to hear from you.  

### Where can I  start?
You can give a look to open issues tab, there you can find  new features and bugs reported.

### Setting up

The app works using the Realtime database, for any doubt check the [docs][firebase_docs_database].
you must  create your own project on firebase console, the structure is below.

```
{
  "busses" : {
    "27_FEBRERO" : {
      "active" : false,
      "created" : 1465147579216,
      "latitude" : 18.48824786,
      "longitude" : -69.9176844,
      "name" : "27 de Febrero"
    },
    "CHARLES" : {
      "active" : false,
      "created" : 14651472579816,
      "latitude" : 18.4884583,
      "longitude" : -69.9176733,
      "name" : "Charles"
    }
  }
}
```

In order to authenticate you must create an user account in the firebase console, Auth section -> tab USER.
 and activated Email/password in  Sign In Method tab

### Pull Request

If you want to contribute make a pull request with your suggestions or new functionality, 
the project moderators will review and test your code, if everything is ok, they will add your changes. **The name of the contributors would be published on the app info :clap:**.



### Code Standards
 - Write code and comments in english
 - Try to follow android patterns and best practice [source 1] [best_practice] [source 2] [performace_android]

### :muscle: Become Repository Moderator 
If we notice that someone is very actives and has remarkable participation in the repo, would receive offer to become a repository moderator. 
 
###  :fire: Related Repository :fire:
- [iOS App] [iOS_repo].
- [Android app][android_repo].
- [Web App] [web_app_repo].
 
## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request and enjoy! :D

### Contributors

Check out all the super awesome contributors at [the contributors page](https://github.com/Amejia481/whereismybus_locationtracking/graphs/contributors). :sparkling_heart:

### Extra Help

If you need help with the repo or you have any question, you can ask to Arturo mejia arturomejiamarmol@gmail.com or Angel Garcia angelrenegarcia13@gmail.com, repository moderators.


[firebase_docs_database]: <https://firebase.google.com/docs/database/android/start/>
[firebase_site]: <https://www.firebase.com/>
[BaaS_info]: <https://en.wikipedia.org/wiki/Mobile_backend_as_a_service/>
[iOS_repo]: <https://github.com/Amejia481/whereIsMyBusiOSClient>
[android_repo]: <https://github.com/Amejia481/whereIsMyBusAndroidClient>
[web_app_repo]: <https://github.com/AngelGarcia13/WhereIsMyBus>
[performace_android]: <https://www.youtube.com/playlist?list=PLWz5rJ2EKKc9CBxr3BVjPTPoDPLdPIFCE>
[best_practice]: <https://www.youtube.com/playlist?list=PLWz5rJ2EKKc-lJo_RGGXL2Psr8vVCTWjM>

:sparkles: **happy coding and have fun** :sparkles:.

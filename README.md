# Trevie - Trending movies companion

<img src="https://cloud.githubusercontent.com/assets/15446842/14937639/fc5402a6-0f05-11e6-8fa9-a9f5f56aac7c.png"/>

Trevie is an user friendly movie discovery application for Android tablets and smartphones running Android 4.0.1 (API level 14) or newer. It demonstrates use of [TheMovieDB API](https://www.themoviedb.org/documentation/api) to create fully featured application.

**Features:**

- Displays grid of movies fetched from [TheMovieDB](https://www.themoviedb.org/)
- Allows users to sort movies by highest rating or popularity
- Shows additional movie information upon tap on a poster
- Plays movie trailers in external app
- Lets users share trailers via share intent
- Stores favourite movies in local database
- Features master-detail tablet layout

## Try it out
To install the app on a connected device or running emulator, run:

```gradle
git clone https://github.com/mattwiduch/Trevie.git
cd Trevie
./gradlew installDevelopDebug
```

**TheMovieDB API Key is required**

In order for the Trevie app to function properly an API key for [TheMovieDB](https://www.themoviedb.org/) must be included with the build. Please include your own key for the build by adding the following line to `[USER_HOME]/.gradle/gradle.properties`

```gradle
MyTheMovieDBApiKey=<"UNIQUE_API_KEY">
```

## Dependencies
Trevie uses following third-party libraries:
- [Picasso](http://square.github.io/picasso/)
- [ButterKnife](http://jakewharton.github.io/butterknife/)

## License
```
Copyright (c) 2015 Mateusz Widuch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

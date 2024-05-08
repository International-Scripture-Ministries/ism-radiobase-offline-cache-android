# capacitor-plugin-offline-cache

Ionic capacitor plugin for Android which load total 66 bible book (New & Old testaments) respectively without internet.

## Installation

```bash
npm install capacitor-plugin-offline-cache
npx cap sync
```

## Post Installation
After building Ionic Capacitor Android platform, copy all of your database files into a following path:
```bash
\android\app\src\main\assets
```


Copy the following code into your MainActivity of your Ionic Capacitor project following 'com.radiobase.radiobase'
```java
package com.radiobase.radiobase;

import android.content.res.AssetManager;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {


  public static final List<String> BOOKS_ID = Arrays.asList("1ch", "1co", "1jn", "1ki", "1pe", "1sa", "1th", "1ti", "2ch", "2co", "2jn", "2ki", "2pe", "2sa", "2th", "2ti", "3jn", "act", "amo", "col", "dan", "deu", "ecc", "eph", "est", "exo", "ezk", "ezr", "gal", "gen", "hab", "hag", "heb", "hos", "isa", "jas", "jdg", "jer", "jhn", "job", "jol", "jon", "jos", "jud", "lam", "lev", "luk", "mal", "mat", "mic", "mrk", "nam", "neh", "num", "oba", "phm", "php", "pro", "psa", "rev", "rom", "rut", "sng", "tit", "zec", "zep");

  private ArrayList<File> myFiles;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String pkgName = this.getApplicationContext().getPackageName();
    myFiles = new ArrayList<>();

    for (String dbNames : BOOKS_ID) {
      myFiles.add(new File("/data/data/" + pkgName + "/databases/", dbNames));
    }

    for (File myFile : myFiles) {
      if (!myFile.exists()) {
        try {
          dumpFileIntoPhone("/data/data/" + pkgName + "/databases/", myFile.getName());
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    }

  }


  private void dumpFileIntoPhone(String FILE_PATH, String FILE_NAME) throws IOException {

    if (!new File(FILE_PATH).exists()) {
      new File(FILE_PATH).mkdir();
    }
    AssetManager assetManager = this.getAssets();

    InputStream myInput = assetManager.open(FILE_NAME);
    String outFileName = FILE_PATH + FILE_NAME;
    OutputStream myOutput = new FileOutputStream(outFileName);
    byte[] buffer = new byte[1024];
    int length;
    while ((length = myInput.read(buffer)) != -1) {
      myOutput.write(buffer, 0, length);
    }
    myOutput.flush();
    myOutput.close();
    myInput.close();
  }
}
```
## Usage

<docgen-index>

* [`action(...)`](#action)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### action(...)

```typescript
action(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

</docgen-api>

## action methods
You have to call action function by passing json string in "value" parameter.
```js
getAllBooks()
// return all books data matching the bibleId

getBibleData(params: book_id)
// return one book data matching book_id.

getVerses(params: bookId, bibleId, chapterNumber, chapterNumber)
// return all the verses data matching the bookId, bibleID & chapterNumber

getBookTeaching(params: book_id)
// return all teachings data matching book_id

getTeaching(params: book_id, teaching_id)
// return one teaching data matching book_id and teaching_id

getTeachings(params: bible_book, chapterNumber, verseNumber)
// return teachings data matching bible_book, chapterNumber and verseNumber

getTotalDownloads()
// return list of book data which has atleast one chapter/teaching audio to be downloaded

getDownloadList(param: book_id, file_type:(chapter/teaching))
// return list of audios to be downloaded matching chapter or teaching

getPercentage(params: book_id, file_type(chapter/teaching))
// return all verses or teaching progress(%) of completion.

getBookPercentage(params: file_type(chapter/teaching))
// return the percentage of completion of total chapters or teachings audio of all books

updateDownload(params: file_name)
// update local downloaded file path to database using file_name.

delete(book_id, file_type:(chapter/teaching), chapterNumber, uuid)
// will delete the specific downloaded content

deleteDownloads(params: book_id, file_type, chapterDownloads, studyDownloads)
// delete local path of one or all downloaded files of chapters and teaching respectively
```

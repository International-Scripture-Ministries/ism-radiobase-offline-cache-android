# capacitor-plugin-offline-cache

Ionic capacitor plugin for Android which load total 66 bible book (New & Old testaments) respectively without internet.

## Installation

```bash
npm install capacitor-plugin-offline-cache
npx cap sync
```

## API

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

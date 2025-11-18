# Sudoku

Sudoku for keyboard warriors. 
 
# Keyboard Controls

| Action               | Key           | Duplicate Key       |
|----------------------|---------------|---------------------|
| New game             | CTRL + INS    |                     |
| Add number           | [0-9]         | NUMPAD[0-9]         |
| Add note             | SHIFT + [0-9] | SHIFT + NUMPAD[0-9] |
| Highlight number     | ALT + [0-9]   | ALT + NUMPAD[0-9]   |
| Undo                 | CTRL + X      |                     |
| Redo                 | CTRL + Z      |                     |
| Clear cell           | DEL           | BACKSPACE           |
| Clear all            | SHIFT + DEL   | SHIFT + BACKSPACE   |
| Auto add all notes   | CTRL + N      |                     |
| Auto add all singles | CTRL + S      |                     |
| Exit                 | ESC           |                     |

### References

###### `Shift + NUMPAD{0-9}` doesn't work on Windows ?

Because Microsoft somehow decided that there's no need for us to be able to turn off
the numpad's default behaviour of when pressing `SHIFT` the numpad keys start sending
other keycodes (e.g. arrow keys, page{up, down}, end etc) and not `NUMPAD{0-9}`.
So adding notes with numpad doesn't work unless you use one of these 
[solutions](https://learn.microsoft.com/en-us/answers/questions/3935239/how-to-make-it-so-left-shift-doesnt-affect-number?forum=windows-all&referrer=answers) 

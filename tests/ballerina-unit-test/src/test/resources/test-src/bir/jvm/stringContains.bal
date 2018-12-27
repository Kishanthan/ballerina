function stringContains(string[] content, string textToSearch) returns int {
    int i = 0;
    int len = lengthof content;
    while (i < len) {
	    string line = content[i];
        if (line.contains(textToSearch)) {
	        return i;
	    }
	    i = i + 1;
    }
    return -1;
}

function foo(string[] content, string textToSearch) returns int {
    int i = 0;
    int result = -1;
    while (i < 100) {
        result = stringContains(content, textToSearch);
        i = i + 1;
    }

    return result;
}

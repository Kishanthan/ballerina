function stringMatch(string[] content, string pattern) returns int {
    int i = 0;
    int len = lengthof content;
    int count = 0;
    while (i < len) {
	    string line = content[i];
        if (check line.matches(pattern)) {
	        count = count + 1;
	    }
	    i = i + 1;
    }
    return count;
}

function foo(string[] content, string pattern) returns int {
    int i = 0;
    int result = -1;
    while (i < 10) {
        result = stringMatch(content, pattern);
        i = i + 1;
    }

    return result;
}

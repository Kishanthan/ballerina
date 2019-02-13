public type JarFile object {
    map<string> manifestAttributes = {};
    string outputJarName;
    map<byte[]> jarEntries = {};

    public function __init(string outputJarName, map<string> manifestAttributes) {
        self.manifestAttributes = manifestAttributes;
        self.outputJarName = outputJarName;
    }

    public function addJarEntry(string name, byte[] content) {
        self.jarEntries[name] = content;
    }

    public extern function create() returns error?;
};
def outputDirectory = new File(basedir, 'target/generated-text')

assert outputDirectory.isDirectory()
assert outputDirectory.listFiles().length == 0

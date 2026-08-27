def lineSeparator = System.lineSeparator()
def expected = "Test-2 (classpath): Test mustache file: receipt is Classpath example for Joe Bloggs${lineSeparator}"

assert new File(basedir, 'target/generated-text/example-2.txt').getText('UTF-8') == expected

def lineSeparator = System.lineSeparator()
def read = { path -> new File(basedir, path).getText('UTF-8') }

assert read('target/generated-text/exampleA-1.txt') ==
        "Test-1: Test mustache file: receipt is Oz-Ware Purchase Invoice for Dorothy Gale${lineSeparator}"
assert read('target/generated-text/exampleB-1.txt') ==
        "Test-1: Test mustache file: receipt is Something Else for Joe Bloggs${lineSeparator}"

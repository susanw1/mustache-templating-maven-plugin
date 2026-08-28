def output = new File(basedir, 'target/generated-text/nested/example.txt')

assert output.isFile()
assert output.getText('UTF-8') == "Nested: valid${System.lineSeparator()}"

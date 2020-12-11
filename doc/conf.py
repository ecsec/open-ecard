# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
import re
import xml.etree.ElementTree as ET
# sys.path.insert(0, os.path.abspath('.'))


# -- Project information -----------------------------------------------------

project = 'Open eCard'
copyright = '2020, ecsec GmbH'
author = 'ecsec gmbH'

# The full version, including alpha/beta/rc tags
pom = ET.parse('../src/pom.xml')
pomVersion = pom.find('./pom:version', {'pom': 'http://maven.apache.org/POM/4.0.0'}).text
version = pomVersion
release = pomVersion

releaseMinor = re.match('^([1-9][0-9]*\.[0-9]+)\.[0-9]+.*', pomVersion).group(1)


# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx-prompt',
    'sphinx_substitution_extensions',
]

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']

numfig = True

# variable substitution in sphinx_substitution_extensions
rst_prolog = """
.. |release| replace:: {release}
.. |releaseMinor| replace:: {releaseMinor}
""".format(release=release, releaseMinor=releaseMinor)

# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'agogo'

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']

html_use_index = False


# -- Options for LaTeX output -------------------------------------------------

latex_logo = 'oec-logo.png'
latex_show_urls = 'footnote'

latex_elements = {
    'extrapackages': r'\usepackage{pdflscape}',
    'papersize': r'a4paper',
}

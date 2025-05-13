/****************************************************************************
 * Copyright (C) 2013-2025 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.richclient.gui.manage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonException
import org.openecard.addon.AddonManager
import org.openecard.addon.AddonProperties
import org.openecard.addon.AddonRegistry
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.manifest.AppExtensionSpecification
import org.openecard.addon.manifest.AppPluginSpecification
import org.openecard.addon.manifest.ProtocolPluginSpecification
import org.openecard.common.AppVersion
import org.openecard.common.I18n
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.richclient.gui.graphics.OecIconType
import org.openecard.richclient.gui.graphics.oecImage
import org.openecard.richclient.gui.manage.addon.DefaultSettingsGroup
import org.openecard.richclient.gui.manage.addon.DefaultSettingsPanel
import org.openecard.richclient.gui.manage.core.AddonPanelBuilder.createConnectionSettingsAddon
import org.openecard.richclient.gui.manage.core.AddonPanelBuilder.createGeneralSettingsAddon
import org.openecard.richclient.gui.manage.core.AddonPanelBuilder.createLogSettingsAddon
import org.openecard.richclient.gui.manage.core.AddonPanelBuilder.createMiddlewareSelectionAddon
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Image
import java.awt.Insets
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.IOException
import java.io.InputStream
import javax.swing.Box
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

private val LOG = KotlinLogging.logger {}

/**
 * Dialog for the management of add-ons and builtin functionality.
 * The dialog hosts a sidebar where one can select the add-on or builtin item to display. The items are
 * [AddonPanel]s which are configured appropriately.
 *
 * @author Tobias Wich
 */
class ManagementDialog(
	manager: AddonManager,
) : JFrame() {
	private val lang: I18n = I18n.getTranslation("addon")
	private val manager: AddonManager
	private val cpReg: AddonRegistry
	private val fileReg: AddonRegistry

	private val selectionPanel: JPanel
	private val contentPane: JPanel
	private lateinit var coreList: JList<String>
	private lateinit var addonList: JList<String>
	private val addonPanel: JPanel
	private var lastImage: JLabel? = null

	/**
	 * Create a ManagementDialog instance.
	 * The preferred way of opening this dialog is the [.showDialog] function which also makes the
	 * dialog visible and only permits one open instance at a time.
	 */
	init {
		LOG.debug { "Creating ManagementDialog." }
		this.manager = manager
		cpReg = manager.builtinRegistry
		fileReg = manager.externalRegistry

		val logo: Image = oecImage(OecIconType.COLORED, 147, 147)
		iconImage = logo
		setTitle(lang.translationForKey("addon.title", AppVersion.name))
		setDefaultCloseOperation(DISPOSE_ON_CLOSE)
		minimumSize = Dimension(640, 420)
		setSize(780, 480)
		contentPane = JPanel()
		contentPane.setBorder(EmptyBorder(5, 5, 5, 5))
		contentPane.setLayout(BorderLayout(0, 0))
		setContentPane(contentPane)

		addonPanel = JPanel(BorderLayout(), true)
		contentPane.add(addonPanel, BorderLayout.CENTER)

		val selectionWrapper: JPanel = JPanel(BorderLayout())
		contentPane.add(selectionWrapper, BorderLayout.WEST)
		selectionPanel = JPanel()
		selectionWrapper.add(selectionPanel, BorderLayout.NORTH)
		selectionWrapper.add(Box.createHorizontalGlue(), BorderLayout.CENTER)

		val selectionLayout: GridBagLayout = GridBagLayout()
		selectionLayout.rowHeights = intArrayOf(0, 0, 0, 0)
		selectionLayout.columnWeights = doubleArrayOf(1.0)
		selectionLayout.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 1.0)
		selectionPanel.setLayout(selectionLayout)

		LOG.debug { "Creating core list." }
		createCoreList()
		LOG.debug { "Creating addon list." }
		createAddonList()
		LOG.debug { "Setup core list." }
		setupCoreList()
		LOG.debug { "Setup addon list." }
		setupAddonList()

		setLocationRelativeTo(null)
		LOG.debug { "Finished creating ManagementDialog." }
	}

	/**
	 * Sets the logo in the main panel describing the current displayed add-on page.
	 * This method should be called when the add-on page is replaced with another one.
	 *
	 * @param logo Image of the logo to display. Must be scaled to size 45x45.
	 */
	fun setLogo(logo: Image) {
		if (lastImage != null) {
			selectionPanel.remove(lastImage)
		}
		val l = JLabel(ImageIcon(logo))
		lastImage = l
		val labelConstraints = GridBagConstraints()
		labelConstraints.insets = Insets(5, 0, 6, 10)
		labelConstraints.anchor = GridBagConstraints.NORTH
		labelConstraints.gridx = 0
		labelConstraints.gridy = 0
		selectionPanel.add(l, labelConstraints)
		selectionPanel.revalidate()
		selectionPanel.repaint()
	}

	private fun createCoreList() {
		val label = JLabel(lang.translationForKey("addon.list.core", AppVersion.name))
		label.setFont(label.font.deriveFont(Font.BOLD))
		val labelConstraints: GridBagConstraints = GridBagConstraints()
		labelConstraints.insets = Insets(5, 0, 5, 10)
		labelConstraints.anchor = GridBagConstraints.NORTH
		labelConstraints.gridx = 0
		labelConstraints.gridy = 1
		selectionPanel.add(label, labelConstraints)

		coreList = JList()
		coreList.setFont(coreList.font.deriveFont(Font.PLAIN))
		coreList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		val coreListConstraints: GridBagConstraints = GridBagConstraints()
		coreListConstraints.fill = GridBagConstraints.HORIZONTAL
		coreListConstraints.insets = Insets(0, 5, 5, 10)
		coreListConstraints.anchor = GridBagConstraints.NORTH
		coreListConstraints.gridx = 0
		coreListConstraints.gridy = 2

		val model: AddonSelectionModel = AddonSelectionModel(this, addonPanel)
		coreList.setModel(model)
		coreList.addListSelectionListener(model)
		addWindowListener(model) // save current addon settings when closed
		// add addon panels
		model.addElement(
			lang.translationForKey("addon.list.core.general"),
			createGeneralSettingsAddon(),
		)
		model.addElement(
			lang.translationForKey("addon.list.core.connection"),
			createConnectionSettingsAddon(),
		)
		model.addElement(
			lang.translationForKey("addon.list.core.logging"),
			createLogSettingsAddon(),
		)
		model.addElement(
			lang.translationForKey("addon.list.core.middleware"),
			createMiddlewareSelectionAddon(),
		)

		// this assumes that all addons in the ClasspathRegistry are core addons
		// an ActionPanel for every addon that has one or more AppExtensionActions will be added
		for (desc: AddonSpecification in cpReg.listAddons()) {
			createAddonPaneFromAddonSpec(desc, model, true)
		}

		selectionPanel.add(coreList, coreListConstraints)
	}

	private fun createAddonList() {
		val label: JLabel = JLabel(lang.translationForKey("addon.list.addon"))
		label.setFont(label.font.deriveFont(Font.BOLD))
		val labelConstraints: GridBagConstraints = GridBagConstraints()
		labelConstraints.insets = Insets(5, 0, 5, 10)
		labelConstraints.anchor = GridBagConstraints.NORTH
		labelConstraints.gridx = 0
		labelConstraints.gridy = 3
		selectionPanel.add(label, labelConstraints)

		// TODO: remove this code
		// label.setVisible(false);
		addonList = JList()
		addonList.setFont(addonList.font.deriveFont(Font.PLAIN))
		addonList.selectionMode = ListSelectionModel.SINGLE_SELECTION
		val addonListConstraints: GridBagConstraints = GridBagConstraints()
		addonListConstraints.fill = GridBagConstraints.HORIZONTAL
		addonListConstraints.insets = Insets(0, 5, 5, 10)
		addonListConstraints.anchor = GridBagConstraints.NORTH
		addonListConstraints.gridx = 0
		addonListConstraints.gridy = 4

		val model: AddonSelectionModel = AddonSelectionModel(this, addonPanel)
		addonList.setModel(model)
		addonList.addListSelectionListener(model)
		addWindowListener(model) // save current addon settings when closed

		// add addon panels

		// this assumes that all addons in the FileRegistry are non core addons
		for (desc: AddonSpecification in fileReg.listAddons()) {
			createAddonPaneFromAddonSpec(desc, model, false)
		}

		selectionPanel.add(addonList, addonListConstraints)
	}

	private fun setupCoreList() {
		coreList.addListSelectionListener(ClearSelectionListener(addonList))
		coreList.setSelectedIndex(0)
	}

	private fun setupAddonList() {
		addonList.addListSelectionListener(ClearSelectionListener(coreList))
	}

	fun updateGui() {
		selectionPanel.removeAll()
		createCoreList()
		createAddonList()
		coreList.setSelectedIndex(0)
	}

	private inner class ClearSelectionListener(
		private val otherList: JList<*>?,
	) : ListSelectionListener {
		override fun valueChanged(e: ListSelectionEvent) {
			if (otherList != null) {
				val source = e.source
				if (source is JComponent) {
					// only do this when we have the focus
					if (!e.valueIsAdjusting && source.hasFocus()) {
						otherList.clearSelection()
					}
				}
			}
		}
	}

	private fun createAddonPaneFromAddonSpec(
		desc: AddonSpecification,
		model: AddonSelectionModel,
		coreAddon: Boolean,
	) {
		val description: String = desc.getLocalizedDescription(LANGUAGE_CODE)
		val name = desc.getLocalizedName(LANGUAGE_CODE)
		var logo: Image? = null

		if (coreAddon) {
			logo = loadLogo(null, desc.logo)
		} else {
			try {
				val loader: ClassLoader = manager.registry.downloadAddon(desc)
				logo = loadLogo(loader, "META-INF/" + desc.logo)
			} catch (ex: AddonException) {
				LOG.error { "Failed to load logo from Add-on bundle." }
				logo = null
			}
		}

		// setup about panel but just if we don't have a core addon
		val about: String = desc.getAbout(LANGUAGE_CODE)
		val licenseText: String = desc.getLicenseText(LANGUAGE_CODE)
		var aboutPanel: AboutPanel? = null
		if ((about != "" || licenseText != "") && !coreAddon) {
			aboutPanel = AboutPanel(desc, false, manager, this)
		}

		// initial setup of settings panel if the addon has general settings in the non protocol/action specific
		// declaration
		var settingsPanel: DefaultSettingsPanel? = null
		val settingsGroups: ArrayList<DefaultSettingsGroup> = ArrayList()
		val addonProps: AddonProperties = AddonProperties(desc)
		val settings: Settings = SettingsFactory.getInstance(addonProps)
		if (desc.configDescription != null && desc.configDescription.entries.isNotEmpty()) {
			val group: DefaultSettingsGroup =
				DefaultSettingsGroup(
					lang.translationForKey("addon.settings.general"),
					settings,
					desc.configDescription.entries,
				)
			settingsGroups.add(group)
		}

		// iteration over the configuration of actions and protocols
		// AppExtensionActions
		if (desc.applicationActions.isNotEmpty()) {
			for (appExtSpec: AppExtensionSpecification in desc.applicationActions) {
				if (appExtSpec.configDescription != null && appExtSpec.configDescription.entries.isNotEmpty()
				) {
					val group: DefaultSettingsGroup =
						DefaultSettingsGroup(
							(
								appExtSpec.getLocalizedName(LANGUAGE_CODE) +
									" " + lang.translationForKey("addon.settings.settings")
							),
							settings,
							appExtSpec.configDescription.entries,
						)
					settingsGroups.add(group)
				}
			}
		}

		// Binding actions
		if (desc.bindingActions.isNotEmpty()) {
			for (appPluginSpec: AppPluginSpecification in desc.bindingActions) {
				if (appPluginSpec.configDescription != null && appPluginSpec.configDescription.entries.isNotEmpty()
				) {
					val group: DefaultSettingsGroup =
						DefaultSettingsGroup(
							appPluginSpec.getLocalizedName(LANGUAGE_CODE) +
								" " + lang.translationForKey("addon.settings.settings"),
							settings,
							appPluginSpec.configDescription.entries,
						)
					settingsGroups.add(group)
				}
			}
		}

		// IFD Actions
		if (desc.ifdActions.isNotEmpty()) {
			for (protPluginSpec: ProtocolPluginSpecification in desc.ifdActions) {
				if (protPluginSpec.configDescription != null && protPluginSpec.configDescription.entries.isNotEmpty()
				) {
					val group: DefaultSettingsGroup =
						DefaultSettingsGroup(
							protPluginSpec.getLocalizedName(LANGUAGE_CODE) +
								" " + lang.translationForKey("addon.settings.settings"),
							settings,
							protPluginSpec.configDescription.entries,
						)
					settingsGroups.add(group)
				}
			}
		}

		// SAL Actions
		if (desc.salActions.isNotEmpty()) {
			for (protPluginSpec: ProtocolPluginSpecification in desc.salActions) {
				if (protPluginSpec.configDescription != null && protPluginSpec.configDescription.entries.isNotEmpty()
				) {
					val group: DefaultSettingsGroup =
						DefaultSettingsGroup(
							(
								protPluginSpec.getLocalizedName(
									LANGUAGE_CODE,
								) +
									" " + lang.translationForKey("addon.settings.settings")
							),
							settings,
							protPluginSpec.configDescription.entries,
						)
					settingsGroups.add(group)
				}
			}
		}

		if (settingsGroups.isNotEmpty()) {
			settingsPanel = DefaultSettingsPanel(*settingsGroups.toTypedArray<DefaultSettingsGroup>())
		}

		// create the actions panel
		var actionPanel: ActionPanel? = null
		if (desc.applicationActions.isNotEmpty()) {
			actionPanel = ActionPanel()
			for (appExtSpec: AppExtensionSpecification in desc.applicationActions) {
				val entry: ActionEntryPanel = ActionEntryPanel(desc, appExtSpec, manager)
				actionPanel.addActionEntry(entry)
			}
		}

		var nextPanel: AddonPanel? = null
		// check whether to use a tabbed pane or not
		if (actionPanel != null && settingsPanel == null && aboutPanel == null) {
			nextPanel = AddonPanel(actionPanel, name, description, logo)
		} else if (actionPanel == null && settingsPanel != null && aboutPanel == null) {
			nextPanel = AddonPanel(settingsPanel, name, description, logo)
		} else if (actionPanel == null && settingsPanel == null && aboutPanel != null) {
			nextPanel = AddonPanel(aboutPanel, name, description, logo)
		} else if (actionPanel != null || settingsPanel != null || aboutPanel != null) {
			nextPanel = AddonPanel(actionPanel, settingsPanel, aboutPanel, name, description, logo)
		}

		if (nextPanel != null) {
			model.addElement(name, nextPanel)
		}
	}

	companion object {
		private const val serialVersionUID: Long = 1L
		private val LANGUAGE_CODE: String = System.getProperty("user.language")

		private var runningDialog: ManagementDialog? = null

		/**
		 * Creates a new instance of the dialog and displays it.
		 * This method only permits a single instance, so this is the preferred way to open the dialog.
		 *
		 * @param manager
		 */
		@Synchronized
		fun showDialog(manager: AddonManager) {
			val rd = runningDialog
			if (rd == null) {
				LOG.debug { "Creating ManagementDialog." }
				val dialog: ManagementDialog = ManagementDialog(manager)
				dialog.addWindowListener(
					object : WindowListener {
						override fun windowOpened(e: WindowEvent) {}

						override fun windowClosing(e: WindowEvent) {}

						override fun windowClosed(e: WindowEvent) {
							runningDialog = null
						}

						override fun windowIconified(e: WindowEvent) {}

						override fun windowDeiconified(e: WindowEvent) {}

						override fun windowActivated(e: WindowEvent) {}

						override fun windowDeactivated(e: WindowEvent) {}
					},
				)
				LOG.debug { "Displaying ManagementDialog." }
				dialog.isVisible = true
				runningDialog = dialog
			} else {
				LOG.debug { "Not displaying ManagementDialog." }
				// dialog already shown, bring to front
				rd.toFront()
			}
		}

		/**
		 * Load the logo from the given path as [Image].
		 *
		 * @param logoPath path to the logo
		 * @return the logo-[Image] if loading was successful, otherwise `null`
		 */
		private fun loadLogo(
			loader: ClassLoader?,
			logoPath: String?,
		): Image? {
			if (logoPath.isNullOrEmpty()) {
				return null
			}
			try {
				val logoStream: InputStream?
				if (loader == null) {
					logoStream = resolveResourceAsStream(ManagementDialog::class.java, logoPath)
				} else {
					logoStream = resolveResourceAsStream(loader, logoPath)
				}

				val icon =
					logoStream?.let {
						ImageIcon(
							toByteArray(
								it,
							),
						)
					}
				return icon?.let {
					if (icon.iconHeight < 0 || icon.iconWidth < 0) {
						// supplied data was no image, btw the image API sucks
						null
					} else {
						icon.image
					}
				}
			} catch (ex: IOException) {
				// ignore and let the default decide
				return null
			}
		}
	}
}

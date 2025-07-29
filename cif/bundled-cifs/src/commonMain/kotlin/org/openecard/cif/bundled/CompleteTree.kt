package org.openecard.cif.bundled

import org.openecard.cif.definition.recognition.RecognitionTree
import org.openecard.cif.dsl.builder.recognition.RecognitionTreeBuilder
import org.openecard.utils.common.hex

object CompleteTree {
	@OptIn(ExperimentalUnsignedTypes::class)
	val calls: RecognitionTree by lazy {
		val b = RecognitionTreeBuilder()
		b.run {
			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4020C020003")
						response {
							call {
								command = hex("00B20104FF")
								response {
									body {
										offset = 0x17u
										length = 0x01u
										value = hex("06")
									}
									call {
										command = hex("00A4000C023F00")
										response {
											call {
												command = hex("00A4010C02AB00")
												response {
													call {
														command = hex("00A4020C021F00")
														response {
															call {
																command = hex("00B00000FF")
																response {
																	body {
																		length = 0x0Du
																		value = hex("A00B8004002000815F2F028011")
																	}
																	call {
																		command = hex("00A4000C023F00")
																		response {
																			call {
																				command = hex("00A4020C020003")
																				response {
																					call {
																						// Check institute and major industry identifier
																						command = hex("00B20104FF")
																						response {
																							body {
																								length = 0x02u
																								value = hex("6726")
																							}
																							// VR Bank Card
																							recognizedCardType("urn:oid:1.3.6.1.4.1.17696.4.3.1.6.1")
																						}
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4020C022F00")
						response {
							call {
								command = hex("00B20404FF")
// 									response {
// 										trailer = 0x6282u
// 										body(0x61u) {
// 											this.matchBytes {
// 												length = 0x0Du
// 												value = hex("4F0BE828BD080FD27600006601")
// 											}
// 										}
// 										recognizedCardType("http://www.baek.de/HBA")
// 									}
								response {
									trailer = 0x6282u
									body {
										length = 0x3Bu
										value =
											hex(
												"61394F08D0400000170013015015476577C3B6686E6C69636865205369676E61" +
													"74757251023F057312300804063F003F055031A00604043F005032",
											)
									}
									recognizedCardType("http://cif.chipkarte.at/e-card/g3")
								}
								response {
									trailer = 0x6282u
									body {
										length = 0x1Bu
										value = hex("61194F08D0400000170018015009496E666F626F78656E51023F06")
									}
									recognizedCardType("http://cif.chipkarte.at/e-card/g4")
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4020C022F11")
						response {
							call {
								command = hex("00B00000FF")
								response {
									trailer = 0x6282u
									// EF Logging Version
									body(0xEFu) {
										matchData(0xC7u) {
											matchBytes {
												length = 0x03u
												value = hex("010000")
											}
										}
									}
									recognizedCardType(EgkCifDefinitions.cardType)
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4020C022F00")
						response {
							call {
								command = hex("00B20304FF")
								response {
									body(0x61u) {
										matchBytes {
											length = 0x0Eu
											value = hex("4F0CA000000063504B43532D3135")
										}
									}
									recognizedCardType("http://www.aekno.de/eAT-light")
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4020C022F00")
						response {
							call {
								command = hex("00B00000FF")
								response {
									trailer = 0x6282u
									body {
										length = 0x5Au
										value =
											hex(
												"61324F0FE828BD080FA000000167455349474E500F434941207A752044462E65" +
													"5369676E5100730C4F0AA000000167455349474E61094F07A00000024710" +
													"01610B4F09E80704007F00070302610C4F0AA000000167455349474E",
											)
									}
									recognizedCardType(NpaDefinitions.cardType)
								}
								response {
									trailer = 0x6282u
									body(0x61u) {
										matchBytes {
											length = 0x08u
											value =
												hex(
													"4F06D27600004002",
												)
										}
									}
									recognizedCardType("http://www.dgn.de/cif/HPCqSIG")
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4040C0FF04573744549442076657220312E30")
				response {
					// TODO: check if this match makes sense
					body {
						length = 0x00u
					}
					recognizedCardType("http://cif.id.ee/eid")
				}
			}

			call {
				command = hex("00A4040C0FD23300000045737445494420763335")
				response {
					// TODO: check if this match makes sense
					body {
						length = 0x00u
					}
					call {
						command = hex("00CA010003")
						response {
							// Version is 03.05.00-07 (The only version having the scheme from 3.5.8+ cards)
							body {
								value = hex("030500")
								mask = hex("FFFFF8")
							}
							recognizedCardType("http://cif.id.ee/eidV3.5")
						}
						response {
							body {
								value = hex("030500")
								mask = hex("FFFF00")
							}
							recognizedCardType("http://cif.id.ee/eidV3.5.8+")
						}
						response {
							body {
								value = hex("0305")
							}
							recognizedCardType("http://cif.id.ee/eidV3.5")
						}
					}
				}
			}

			call {
				command = hex("00A4000C023F00")
				response {
					call {
						command = hex("00A4010C025015")
						response {
							call {
								command = hex("00A4020C025032")
								response {
									call {
										command = hex("00B00000FF")
										response {
											trailer = 0x6282u
											body {
												offset = 0x1Bu
												length = 0x1Eu
												value =
													hex("442D545255535420436172642056332E30207374616E6461726420326765")
											}
											recognizedCardType("http://www.ihk.de/cif")
										}
										response {
											trailer = 0x6282u
											body {
												offset = 0x1Bu
												length = 0x1Au
												value = hex("442D545255535420436172642056332E30207374616E64617264")
											}
											recognizedCardType("https://www.d-trust.net/produkte/d-trust-signaturkarten/d-trust-card/standard_v3")
										}
										response {
											trailer = 0x6282u
											body {
												offset = 0x1Bu
												length = 0x17u
												value = hex("442D545255535420436172642056332E30206D756C7469")
											}
											recognizedCardType("https://www.d-trust.net/produkte/d-trust-signaturkarten/d-trust-card/multi_v3")
										}
										response {
											trailer = 0x6282u
											body {
												offset = 0x1Bu
												length = 0x17u
												value = hex("442D545255535420436172642056332E30206261746368")
											}
											recognizedCardType("https://www.d-trust.net/produkte/d-trust-signaturkarten/d-trust-card/batch_v3")
										}
									}
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4080C063F00DF005032")
				response {
					call {
						command = hex("00B0001906")
						response {
							body {
								length = 0x06u
								value = hex("42454C504943")
							}
							call {
								command = hex("80E400001C")
								response {
									body {
										offset = 0x15u
										length = 0x01u
										value = hex("11")
									}
									recognizedCardType("http://eid.belgium.be/cif/v1-0-1")
								}
								response {
									body {
										offset = 0x15u
										length = 0x01u
										value = hex("17")
									}
									recognizedCardType("http://eid.belgium.be/cif/v1-7-0")
								}
							}
						}
					}
				}
			}

			call {
				command = hex("00A4040400")
				response {
// 					body(0x62u) {
// 						matchData(0x84u) {
// 							matchBytes { value = hex("D27600014601") }
// 						}
// 					}
					body {
						offset = 0x10u
						length = 0x06u
						value = hex("D27600014601")
					}
					recognizedCardType(HbaDefinitions.cardType)
				}
			}
		}
		b.build()
	}
}

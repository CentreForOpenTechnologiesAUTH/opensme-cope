/*
 * Copyright (C) 2009 VTT Technical Research Centre of Finland.
 *
 * This file is part of NOEN framework.
 *
 * NOEN framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2.
 *
 * NOEN framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package fi.vtt.noen.testgen.model.efsm;

import java.util.Comparator;

/**
 * This is used in code generation to order the generated import statements in the resulting source code.
 *
 * @author Teemu Kanstron
 */
public class ImportClassComparator implements Comparator<Class> {
  public int compare(Class c1, Class c2) {
    return c1.getName().compareTo(c2.getName());
  }
}

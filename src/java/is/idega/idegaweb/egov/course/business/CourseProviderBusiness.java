/**
 * @(#)CourseProviderBusiness.java    1.0.0 10:09:09 AM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between
 * Idega Software hf., a business formed and operating under laws
 * of Iceland, having its principal place of business in Reykjavik,
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source
 *     code that may be made available according to the documentation for
 *     a particular software product (Software) from Manufacturer
 *     (Source Code) shall be provided to Licensee, provided that
 *     (1) funds have been received for payment of the License for Software and
 *     (2) the appropriate License has been purchased as stated in the
 *     documentation for Software. As used in this License Agreement,
 *     Licensee shall also mean the individual using or installing
 *     the source code together with any individual or entity, including
 *     but not limited to your employer, on whose behalf you are acting
 *     in using or installing the Source Code. By completing this agreement,
 *     Licensee agrees to be bound by the terms and conditions of this Source
 *     Code License Agreement. This Source Code License Agreement shall
 *     be an extension of the Software License Agreement for the associated
 *     product. No additional amendment or modification shall be made
 *     to this Agreement except in writing signed by Licensee and
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to
 *     Licensee a non-transferable, worldwide license during the term of
 *     this Agreement to use the Source Code for the associated product
 *     purchased. In the event the Software License Agreement to the
 *     associated product is terminated; (1) Licensee's rights to use
 *     the Source Code are revoked and (2) Licensee shall destroy all
 *     copies of the Source Code including any Source Code used in
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the
 *         Source Code alone, it shall only be distributed as a
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code
 *         provided by this this Source Code License Agreement.
 *         All Source Code provided by this Agreement that is used
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet),
 *         must be protected to the extent that it cannot be easily
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute
 *         the products created from the Source Code in any way that
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains
 *     proprietary information. Licensee shall not distribute or
 *     reveal the Source Code to anyone other than the software
 *     developers of Licensee's organization. Licensee may be held
 *     legally responsible for any infringement of intellectual property
 *     rights that is caused or encouraged by Licensee's failure to abide
 *     by the terms of this Agreement. Licensee may make copies of the
 *     Source Code provided the copyright and trademark notices are
 *     reproduced in their entirety on the copy. Manufacturer reserves
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the
 *     Source Code is correct, reliable, date compliant, and technically
 *     accurate, the Source Code is licensed to Licensee as is and without
 *     warranties as to performance of merchantability, fitness for a
 *     particular purpose or use, or any other warranties whether
 *     expressed or implied. Licensee's organization and all users
 *     of the source code assume all risks when using it. The manufacturers,
 *     distributors and resellers of the Source Code shall not be liable
 *     for any consequential, incidental, punitive or special damages
 *     arising out of the use of or inability to use the source code or
 *     the provision of or failure to provide support services, even if we
 *     have been advised of the possibility of such damages. In any case,
 *     the entire liability under any provision of this agreement shall be
 *     limited to the greater of the amount actually paid by Licensee for the
 *     Software or 5.00 USD. No returns will be provided for the associated
 *     License that was purchased to become eligible to receive the Source
 *     Code after Licensee receives the source code.
 */
package is.idega.idegaweb.egov.course.business;

import is.idega.idegaweb.egov.course.CourseConstants;
import is.idega.idegaweb.egov.course.data.CourseProvider;
import is.idega.idegaweb.egov.course.data.CourseProviderArea;
import is.idega.idegaweb.egov.course.data.CourseProviderCategory;
import is.idega.idegaweb.egov.course.data.CourseProviderType;
import is.idega.idegaweb.egov.course.data.CourseProviderUser;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.idega.business.IBOService;
import com.idega.data.IDOHome;
import com.idega.user.data.Group;
import com.idega.user.data.User;

/**
 * <p>It eats all {@link Exception}s and {@link IDOHome} searching</p>
 * <p>You can report about problems to:
 * <a href="mailto:martynas@idega.is">Martynas Stakė</a></p>
 *
 * @version 1.0.0 Oct 23, 2013
 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
 */
public interface CourseProviderBusiness extends IBOService {

	public static final String PARAMETER_ROOT_ADULT_EDUCATION_ADMINISTRATORS_GROUP = "adult_education_administrators_group_id";

	public static final String PARAMETER_ROOT_HIGH_SCHOOL_ADMINISTRATORS_GROUP = "high_school_administrators_group_id";

	public static final String PARAMETER_ROOT_SCHOOL_ADMINISTRATORS_GROUP = "school_administrators_group_id";

	public static final String ROOT_SCHOOL_ADMINISTRATORS_GROUP = "provider_administrators_group_id";

	public static final String ROOT_MUSIC_SCHOOL_ADMINISTRATORS_GROUP = "music_administrators_group_id";

	public static final String ROOT_ADULT_EDUCATION_ADMINISTRATORS_GROUP = "adult_education_administrators_group_id";
	
	/**
	 *
	 * @param primaryKey is {@link CourseProvider#getPrimaryKey()}, not <code>null</code>;
	 * @return entity by criteria or <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public CourseProvider getSchool(Object primaryKey);

	/**
	 *
	 * @param types to search by, not <code>null</code>;
	 * @return entities by criteria or {@link Collections#emptyList()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public <P extends CourseProvider> Collection<P> findAllSchoolsByType(Collection<? extends CourseProviderType> types);

	/**
	 * <p>FIXME Optimize number of queries, but don't crack possibility
	 * to use in inherited classes</p>
	 * @param area is {@link CourseProviderArea#getPrimaryKey()};
	 * @param type is {@link CourseProviderType#getPrimaryKey()};
	 * @return entities by given criteria or {@link Collections#emptyList()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Collection<? extends CourseProvider> findAllSchoolsByAreaAndType(
			int area, int type);

	/**
	 *
	 * @param area to search by, not <code>null</code>;
	 * @param type to search by, not <code>null</code>;
	 * @return entities by given criteria or {@link Collections#emptyList()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public <P extends CourseProvider> Collection<P> findAllSchoolsByAreaAndType(
			CourseProviderArea area, CourseProviderType type);

	/**
	 *
	 * @param primaryKey is {@link CourseProviderArea#getPrimaryKey()},
	 * not <code>null</code>;
	 * @return entity by primary key or <code>null</code>;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public CourseProviderArea getSchoolArea(Object primaryKey);

	/**
	 *
	 * @param primaryKey is {@link CourseProviderType#getPrimaryKey()},
	 * not <code>null</code>;
	 * @return entity by primary key, <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public CourseProviderType getSchoolType(Object primaryKey);

	/**
	 * <p>FIXME Optimize number of queries, but don't crack possibility
	 * to use in inherited classes</p>
	 * @param category {@link CourseProviderCategory#getCategory()} to search by,
	 * not <code>null</code>;
	 * @return entities or {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public <T extends CourseProviderType> Collection<T> findAllSchoolTypesInCategory(String category);

	/**
	 * 
	 * @return {@link CourseProviderCategory#CATEGORY_AFTER_SCHOOL_CARE} and
	 * creates an entity in the database, if not exists;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public String getAfterSchoolCareSchoolCategory();

	/**
	 * 
	 * @return {@link CourseProviderCategory#CATEGORY_AFTER_SCHOOL_CARE} and
	 * creates an entity in the database, if not exists;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public CourseProviderCategory getCategoryAfterSchoolCare();

	/**
	 *
	 * @param school to search by, not <code>null</code>;
	 * @return entities by criteria or {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Collection<? extends CourseProviderUser> getAllSchoolUsers(CourseProvider school);

	/**
	 * 
	 * <p>Creates (if not available) the default {@link Group} 
	 * of {@link User}s who are school administrators.</p>
	 * @return {@link Group} of school administrators or <code>null</code>
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Group getRootSchoolAdministratorGroup();

	/**
	 * 
	 * <p>Creates (if not available) the default {@link Group} of 
	 * {@link User}s who are high school administrators.</p>
	 * @return {@link Group} of high school administrators or 
	 * <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Group getRootHighSchoolAdministratorGroup();

	/**
	 * 
	 * <p>Creates (if not available) the default {@link Group} for {@link User}s
	 * who are adult education administrators</p>
	 * @return {@link Group} of adult education administrators or <code>null</code>
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Group getRootAdultEducationAdministratorGroup();

	/**
	 * 
	 * <p>Creates (if not available) the default {@link Group} 
	 * of {@link User}s who are administrators of child care 
	 * {@link CourseProvider}s</p>
	 * @return entity or <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Group getRootProviderAdministratorGroup();

	/**
	 * 
	 * @param courseProviders to get {@link CourseProviderType}s for, 
	 * not <code>null</code>;
	 * @return {@link CourseProviderType}s for given {@link CourseProvider}s
	 * or {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProviderType> getCourseProviderTypes(
			Collection<? extends CourseProvider> courseProviders);

	/**
	 * 
	 * <p>Sorts given {@link CourseProvider}s by {@link CourseProviderArea}s
	 * they belong to.</p>
	 * @param courseProviders to sort, not <code>null</code>;
	 * @return sorted {@link CourseProvider}s or {@link Collections#emptyMap()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Map<CourseProviderArea, List<CourseProvider>> getProvidersByAreas(
			Collection<CourseProvider> courseProviders);

	/**
	 * 
	 * <p>Searches for sub-types of {@link CourseProviderUser}s by given
	 * user, then finds all sub-types of {@link CourseProvider}s, which
	 * are connected to those {@link CourseProviderUser}s. Method is
	 * very heavy, use it responsibly...</p>
	 * @param user to get {@link CourseProvider}s for, not <code>null</code>;
	 * @return {@link CourseProvider}s found for {@link User} or 
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersForUser(User user);
	
	/**
	 * 
	 * <p>Searches for sub-types of {@link CourseProviderUser}s by given
	 * user, then finds all sub-types of {@link CourseProvider}s, which
	 * are connected to those {@link CourseProviderUser}s. Method is
	 * very heavy, use it responsibly...</p>
	 * @param user to get {@link CourseProvider}s for, not <code>null</code>;
	 * @param type is {@link CourseProviderType#getPrimaryKey()}, skipped
	 * if <code>null</code>;
	 * @return {@link CourseProvider}s found for {@link User} or 
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersByUserAndType(User user, String type);

	/**
	 * 
	 * <p>Searches for sub-types of {@link CourseProvider} by criteria:</p>
	 * @param type is {@link CourseProviderType#getPrimaryKey()}, 
	 * skipped if <code>null</code>;
	 * @return {@link CourseProvider}s by given criteria or 
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersByType(String type);

	/**
	 * 
	 * <p>Searches for sub-types of {@link CourseProvider} by criteria:</p>
	 * @param type to filter by, skipped if <code>null</code>;
	 * @return {@link CourseProvider}s by given criteria or 
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersByType(CourseProviderType type);

	/**
	 * 
	 * @param user to get {@link CourseProvider}s for, not <code>null</code>;
	 * @return {@link CourseProvider}s or {@link Collections#emptyList()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	<P extends CourseProvider> Collection<P> getHandledCourseProviders(User user);

	/**
	 * 
	 * @return <code>true</code> if current {@link User} has role 
	 * {@link CourseConstants#SUPER_ADMINISTRATOR_ROLE_KEY}
	 * or is system administrator, <code>false</code> otherwise.
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	boolean hasFullAccessRights();

	/**
	 * 
	 * @param type to filter by, skipped if <code>null</code>;
	 * @return {@link CourseProviderArea}s visible for current {@link User} or
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProviderArea> getAreasForCurrentUser(CourseProviderType type);

	/**
	 * 
	 * @return {@link CourseProvider}s visible for current {@link User} or
	 * {@link Collections#emptyList()} on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersForCurrentUser();

	/**
	 * 
	 * @param type to filter by, all {@link CourseProvider}s will be returned
	 * if <code>null</code>;
	 * @return {@link CourseProvider}s visible for current {@link User} or
	 * {@link Collections#emptyList()} on failure; 
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Collection<CourseProvider> getProvidersForCurrentUser(CourseProviderType type);

	/**
	 * 
	 * @param type to filter by, all {@link CourseProvider}s will be returned
	 * if <code>null</code>;
	 * @return sorted {@link CourseProvider}s by {@link CourseProviderArea}s
	 * and filterd by {@link CourseProviderType} or {@link Collections#emptyMap()}
	 * on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	Map<CourseProviderArea, List<CourseProvider>> getProvidersByAreasForCurrentUser(
			CourseProviderType type);
}

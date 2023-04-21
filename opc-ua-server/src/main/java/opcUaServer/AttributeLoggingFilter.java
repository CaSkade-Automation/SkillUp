package opcUaServer;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.GetAttributeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext.SetAttributeContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to monitor skill parameter and update their values
 */
public class AttributeLoggingFilter implements AttributeFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Predicate<AttributeId> attributePredicate;
	private Field field;
	private Object skill;

	public AttributeLoggingFilter() {
		this(attributeId -> true);
	}

	public AttributeLoggingFilter(Predicate<AttributeId> attributePredicate) {
		this.attributePredicate = attributePredicate;
	}

	/**
	 * Constructor of class {@link AttributeLoggingFilter}
	 * 
	 * @param attributePredicate Filter logic to apply
	 * @param field              field whose value has to be updated
	 * @param skill              instance of skill to get field
	 */
	public AttributeLoggingFilter(Predicate<AttributeId> attributePredicate, Field field, Object skill) {
		this.attributePredicate = attributePredicate;
		this.field = field;
		this.skill = skill;
	}

	/**
	 * When an attribute (here skill parameter) is queried, then the value of the
	 * corresponding skills field is updated to actual value of skill parameter in
	 * OpcUa
	 */
	@Override
	public Object getAttribute(GetAttributeContext ctx, AttributeId attributeId) {
		Object value = ctx.getAttribute(attributeId);

		// only log external reads
		if (!attributePredicate.test(attributeId) || !ctx.getSession().isPresent())
			return value;
		DataValue dataValue = (DataValue) value;
		try {
//			setField(field, field.getType(), dataValue, skill);
			field.set(skill, dataValue.getValue().getValue());

		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * When an attribute (here skill parameter) is set, then the value of the
	 * corresponding skills field is updated to actual value of skill parameter in
	 * OpcUa
	 */
	@Override
	public void setAttribute(SetAttributeContext ctx, AttributeId attributeId, Object value) {
		// only log external writes
		if (!attributePredicate.test(attributeId) || !ctx.getSession().isPresent())
			return;
		DataValue dataValue = (DataValue) value;
		try {
//			setField(field, field.getType(), dataValue, skill);
			field.set(skill, dataValue.getValue().getValue());
			logger.info("set nodeId={} attributeId={} value={}", ctx.getNode().getNodeId(), attributeId, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ctx.setAttribute(attributeId, value);
	}

	/**
	 * Method to set value of field to actual value in OpcUa
	 * 
	 * @param field     field whose value should be changed
	 * @param type      data type of field
	 * @param dataValue to get actual value
	 * @param skill     instance of skill to get field
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
//	public void setField(Field field, Class<?> type, DataValue dataValue, Object skill)
//			throws IllegalArgumentException, IllegalAccessException {
//		if (type == boolean.class) {
//			field.set(skill, (boolean) dataValue.getValue().getValue());
//		} else if (type == byte.class) {
//			field.set(skill, (byte) dataValue.getValue().getValue());
//		} else if (type == short.class) {
//			field.set(skill, (short) dataValue.getValue().getValue());
//		} else if (type == int.class) {
//			field.set(skill, (int) dataValue.getValue().getValue());
//		} else if (type == long.class) {
//			field.set(skill, (long) dataValue.getValue().getValue());
//		} else if (type == float.class) {
//			field.set(skill, (float) dataValue.getValue().getValue());
//		} else if (type == double.class) {
//			field.set(skill, (double) dataValue.getValue().getValue());
//		}
//	}
}
